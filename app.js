const API_BASE = 'https://api.kordis.fr/'
const SNAPSHOT_KEY = 'myges.real.snapshot'

const data = {
  profile: null,
  years: [],
  agenda: [],
  grades: [],
  absences: [],
  projects: [],
  documents: [],
  events: [],
  news: [],
  updatedAt: null
}

const state = {
  year: '',
  gradePeriod: 'all',
  absenceStatus: 'all',
  projectQuery: '',
  documentType: 'all',
  eventFilter: 'all'
}

const routes = {
  dashboard: { title: 'Accueil', render: renderDashboard },
  agenda: { title: 'Plannings', render: renderAgenda },
  grades: { title: 'Notes', render: renderGrades },
  absences: { title: 'Absences', render: renderAbsences },
  projects: { title: 'Projets', render: renderProjects },
  documents: { title: 'Documents', render: renderDocuments },
  events: { title: 'Événements', render: renderEvents }
}

const loginForm = document.querySelector('#login-form')
const loginButton = document.querySelector('.login-button')
const tokenInput = document.querySelector('#token-input')
const loginError = document.querySelector('#login-error')
const loginStatus = document.querySelector('#login-status')
const view = document.querySelector('#view')
const pageTitle = document.querySelector('#page-title')
const breadcrumb = document.querySelector('#breadcrumb')
const profileInitials = document.querySelector('#profile-initials')
const profileName = document.querySelector('#profile-name')
const navItems = [...document.querySelectorAll('[data-route]')]

loginForm.addEventListener('submit', async event => {
  event.preventDefault()
  await login()
})
loginButton.addEventListener('click', async event => {
  event.preventDefault()
  await login()
})
view.addEventListener('change', updateFilter)
view.addEventListener('input', updateFilter)
window.addEventListener('hashchange', render)
window.mygesLogin = login
restoreSnapshot()
render()

async function login() {
  hideLoginError()
  const token = tokenInput.value.trim().replace(/^bearer\s+/i, '')
  if (!token) {
    showLoginError('Colle ton bearer Kordis/MyGES pour charger de vraies donnees.')
    return
  }

  loginForm.classList.add('is-loading')
  showLoginStatus('Connexion a Kordis...')
  try {
    await loadRealData(token)
    sessionStorage.setItem(SNAPSHOT_KEY, JSON.stringify(data))
    if (!location.hash || location.hash === '#login') location.hash = '#dashboard'
    document.body.classList.add('is-authenticated')
    render()
  } catch (error) {
    showLoginError("Connexion impossible avec ce bearer. Verifie le token ou l'acces CORS de l'API Kordis.")
  } finally {
    hideLoginStatus()
    loginForm.classList.remove('is-loading')
  }
}

function showLoginError(message) {
  loginError.textContent = message
  loginError.hidden = false
}

function hideLoginError() {
  loginError.hidden = true
  loginError.textContent = ''
}

function showLoginStatus(message) {
  loginStatus.textContent = message
  loginStatus.hidden = false
}

function hideLoginStatus() {
  loginStatus.hidden = true
  loginStatus.textContent = ''
}

async function loadRealData(token) {
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${token}`
  }
  const get = path => fetch(`${API_BASE}${path}`, { headers })
    .then(response => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      return response.json()
    })
    .then(unwrap)

  data.profile = profileFromApi(await get('me/profile'))
  data.years = toArray(await get('me/years')).map(yearValue).filter(Boolean)
  state.year = state.year || data.years[0] || String(new Date().getFullYear())

  const start = Math.floor((Date.now() - 7 * 86400000) / 1000)
  const end = Math.floor((Date.now() + 28 * 86400000) / 1000)
  const [
    agenda,
    grades,
    absences,
    projects,
    documents,
    news,
    events
  ] = await Promise.all([
    get(`me/agenda?start=${start}&end=${end}`).catch(() => []),
    get(`me/${state.year}/grades`).catch(() => []),
    get(`me/${state.year}/absences`).catch(() => []),
    get(`me/${state.year}/projects`).catch(() => []),
    get('me/annualDocuments').catch(() => []),
    get('me/news').catch(() => []),
    get('me/events').catch(() => [])
  ])

  data.agenda = toArray(agenda)
  data.grades = toArray(grades)
  data.absences = toArray(absences)
  data.projects = toArray(projects)
  data.documents = toArray(documents)
  data.news = toArray(news)
  data.events = toArray(events)
  data.updatedAt = Date.now()
  updateProfileHeader()
}

function restoreSnapshot() {
  const saved = sessionStorage.getItem(SNAPSHOT_KEY)
  if (!saved) return
  try {
    Object.assign(data, JSON.parse(saved))
    state.year = state.year || data.years?.[0] || ''
    updateProfileHeader()
  } catch {
    sessionStorage.removeItem(SNAPSHOT_KEY)
  }
}

function updateFilter(event) {
  const key = event.target.dataset.filter
  if (!key) return
  state[key] = event.target.value
  render()
}

function render() {
  const route = location.hash.replace('#', '') || 'login'
  if (!data.profile || route === 'login') {
    document.body.classList.remove('is-authenticated')
    return
  }

  document.body.classList.add('is-authenticated')
  const screen = routes[route] || routes.dashboard
  pageTitle.textContent = screen.title
  breadcrumb.textContent = `Root / ${screen.title}`
  navItems.forEach(item => item.classList.toggle('is-active', item.dataset.route === route))
  view.innerHTML = screen.render()
}

function renderDashboard() {
  const nextCourse = sortedAgenda()[0]
  return `
    <section class="grid dashboard-grid">
      <div class="stack">
        <article class="panel welcome">
          <div>
            <p class="eyebrow">Bonjour ${escapeHtml(firstName(data.profile.name))}</p>
            <h2>${escapeHtml(data.profile.program || 'Espace etudiant')}</h2>
            <p>${escapeHtml(data.profile.school || 'MyGES')} · donnees chargees depuis ton compte Kordis.</p>
            <div class="pill-row">
              <span class="pill">${nextCourse ? `Prochain cours ${formatDateTime(eventStart(nextCourse))}` : 'Aucun cours renvoye'}</span>
              <span class="pill">${data.grades.length} matieres</span>
              <span class="pill">${data.absences.length} absences</span>
            </div>
          </div>
          <div class="campus-card" aria-hidden="true"></div>
        </article>
        <section class="grid cols-3">
          ${metric('Planning', String(data.agenda.length), 'elements renvoyes')}
          ${metric('Notes', String(data.grades.length), 'matieres renvoyees')}
          ${metric('Projets', String(data.projects.length), 'projets renvoyes')}
        </section>
        <section class="grid portal-grid">
          ${portalTile('Plannings', 'Semaine courante', `${data.agenda.length} elements`)}
          ${portalTile('Notes et absences', state.year || 'Annee', `${data.grades.length} notes, ${data.absences.length} absences`)}
          ${portalTile('Documents', 'Scolarite', `${data.documents.length} documents`)}
          ${portalTile('Projets pédagogiques', 'Travaux', `${data.projects.length} projets`)}
        </section>
        <section class="panel">
          <h2>Aujourd'hui</h2>
          <div class="list">${renderList(sortedAgenda().slice(0, 5), agendaItem)}</div>
        </section>
      </div>
      <aside class="stack">
        <section class="panel">
          <h2>Dernieres notes</h2>
          <div class="list">${renderList(data.grades.slice(0, 4), gradeCard)}</div>
        </section>
        <section class="panel">
          <h2>Actualites</h2>
          <div class="list">${renderList(data.news.slice(0, 4), newsCard)}</div>
        </section>
      </aside>
    </section>
  `
}

function renderAgenda() {
  return `
    <section class="stack">
      <div class="filter-bar">
        <strong>${data.agenda.length} elements de planning</strong>
        <span class="muted">Mise a jour ${formatDateTime(data.updatedAt)}</span>
      </div>
      <section class="panel">
        <h2>Planning</h2>
        <div class="list">${renderList(sortedAgenda(), agendaItem)}</div>
      </section>
    </section>
  `
}

function renderGrades() {
  const periods = ['all', ...unique(data.grades.map(gradePeriod))]
  const grades = data.grades.filter(grade => state.gradePeriod === 'all' || gradePeriod(grade) === state.gradePeriod)
  return `
    <section class="filter-bar">
      ${selectField('Période', 'gradePeriod', periods)}
      <span class="muted">${grades.length} matieres</span>
    </section>
    <section class="grid cols-2">
      ${metric('Matieres', String(grades.length), 'renvoyees par MyGES')}
      ${metric('ECTS', String(sum(grades.map(gradeEcts))), 'total affiche')}
    </section>
    <section class="panel">
      <h2>Notes</h2>
      ${gradesTable(grades)}
    </section>
  `
}

function renderAbsences() {
  const absences = data.absences.filter(absence =>
    state.absenceStatus === 'all' || absenceStatus(absence) === state.absenceStatus
  )
  return `
    <section class="filter-bar">
      ${selectField('Justifie', 'absenceStatus', ['all', 'Oui', 'Non'])}
      <span class="muted">${absences.length} absences</span>
    </section>
    <section class="panel">
      <h2>Historique</h2>
      <div class="list">${renderList(absences, absenceItem)}</div>
    </section>
  `
}

function renderProjects() {
  const query = state.projectQuery.toLowerCase()
  const projects = data.projects.filter(project => projectTitle(project).toLowerCase().includes(query))
  return `
    <section class="filter-bar">
      <label class="filter-field">
        Recherche
        <input data-filter="projectQuery" value="${escapeHtml(state.projectQuery)}" placeholder="matiere, projet...">
      </label>
      <span class="muted">${projects.length} projets</span>
    </section>
    <section class="panel">
      <h2>Projets pédagogiques</h2>
      ${projectsTable(projects)}
    </section>
  `
}

function renderDocuments() {
  const documents = data.documents.filter(document =>
    state.documentType === 'all' || documentType(document) === state.documentType
  )
  const types = ['all', ...unique(data.documents.map(documentType))]
  return `
    <section class="filter-bar">
      ${selectField('Type', 'documentType', types)}
      <span class="muted">${documents.length} documents</span>
    </section>
    <section class="panel">
      <h2>Documents</h2>
      <div class="list">${renderList(documents, documentItem)}</div>
    </section>
  `
}

function renderEvents() {
  const events = data.events.filter(event =>
    state.eventFilter === 'all' || eventType(event) === state.eventFilter
  )
  const types = ['all', ...unique(data.events.map(eventType))]
  return `
    <section class="filter-bar">
      ${selectField('Catégorie', 'eventFilter', types)}
      <span class="muted">${events.length} evenements</span>
    </section>
    <section class="panel">
      <h2>Vie étudiante</h2>
      <div class="list">${renderList(events, eventItem)}</div>
    </section>
  `
}

function gradesTable(grades) {
  if (!grades.length) return emptyState()
  return `
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>Matiere</th>
            <th>Intervenant</th>
            <th>Coef.</th>
            <th>ECTS</th>
            <th>Exam</th>
            <th>Moyenne</th>
          </tr>
        </thead>
        <tbody>
          ${grades.map(grade => `
            <tr>
              <td>${escapeHtml(gradeTitle(grade))}</td>
              <td>${escapeHtml(teacherName(grade))}</td>
              <td>${escapeHtml(valueOf(grade, ['coef']) || '-')}</td>
              <td>${escapeHtml(valueOf(grade, ['ects']) || '-')}</td>
              <td>${escapeHtml(valueOf(grade, ['exam', 'letter_mark']) || '-')}</td>
              <td>${escapeHtml(valueOf(grade, ['average', 'ccaverage']) || '-')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `
}

function projectsTable(projects) {
  if (!projects.length) return emptyState()
  return `
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Matiere</th>
            <th>Projet</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${projects.map(project => `
            <tr>
              <td>${escapeHtml(formatDateTime(valueOf(project, ['update_date', 'project_create_date'])))}</td>
              <td>${escapeHtml(valueOf(project, ['course_name']) || '-')}</td>
              <td>${escapeHtml(projectTitle(project))}</td>
              <td><span class="tag">${escapeHtml(project.groups?.length ? 'Groupe' : 'Voir')}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `
}

function agendaItem(event) {
  return `
    <article class="event-card">
      <span class="time-chip">${escapeHtml(formatTime(eventStart(event)))}<br>${escapeHtml(formatTime(eventEnd(event)))}</span>
      <div>
        <h3>${escapeHtml(eventTitle(event))}</h3>
        <span class="muted">${escapeHtml(eventRoom(event) || '-')}</span>
      </div>
      <span class="tag">${escapeHtml(eventType(event) || 'Cours')}</span>
    </article>
  `
}

function gradeCard(grade) {
  return `
    <article class="table-row">
      <div>
        <h3>${escapeHtml(gradeTitle(grade))}</h3>
        <span class="muted">${escapeHtml(teacherName(grade))}</span>
      </div>
      <strong class="score">${escapeHtml(valueOf(grade, ['average', 'ccaverage']) || '-')}</strong>
    </article>
  `
}

function absenceItem(absence) {
  return `
    <article class="table-row">
      <div>
        <h3>${escapeHtml(valueOf(absence, ['course', 'course_name', 'matter']) || 'Absence')}</h3>
        <span class="muted">${escapeHtml(formatDateTime(valueOf(absence, ['date', 'start_date', 'starts_at'])) || '-')}</span>
      </div>
      <span class="tag">${escapeHtml(absenceStatus(absence))}</span>
    </article>
  `
}

function documentItem(document) {
  return `
    <article class="table-row doc-row">
      <span class="doc-icon">DOC</span>
      <div>
        <h3>${escapeHtml(valueOf(document, ['name', 'title', 'label']) || 'Document')}</h3>
        <span class="muted">${escapeHtml(documentType(document))}</span>
      </div>
      <span class="tag">${escapeHtml(valueOf(document, ['status', 'year']) || 'MyGES')}</span>
    </article>
  `
}

function eventItem(event) {
  return `
    <article class="table-row">
      <div>
        <h3>${escapeHtml(valueOf(event, ['name', 'title', 'label']) || 'Evenement')}</h3>
        <span class="muted">${escapeHtml(valueOf(event, ['place', 'location', 'campus']) || '-')}</span>
      </div>
      <span class="tag">${escapeHtml(formatDateTime(valueOf(event, ['date', 'start_date', 'starts_at'])) || eventType(event))}</span>
    </article>
  `
}

function newsCard(news) {
  return `
    <article class="table-row">
      <div>
        <h3>${escapeHtml(valueOf(news, ['title', 'name', 'label']) || 'Actualite')}</h3>
        <span class="muted">${escapeHtml(valueOf(news, ['summary', 'content', 'description']) || '')}</span>
      </div>
    </article>
  `
}

function metric(label, value, detail) {
  return `
    <article class="metric">
      <small>${escapeHtml(label)}</small>
      <strong>${escapeHtml(value)}</strong>
      <em>${escapeHtml(detail)}</em>
    </article>
  `
}

function selectField(label, key, options) {
  return `
    <label class="filter-field">
      ${escapeHtml(label)}
      <select data-filter="${key}">
        ${options.filter(Boolean).map(option => `<option value="${escapeHtml(option)}"${state[key] === option ? ' selected' : ''}>${escapeHtml(option === 'all' ? 'Tous' : option)}</option>`).join('')}
      </select>
    </label>
  `
}

function portalTile(title, detail, meta) {
  const route = title.startsWith('Planning') ? 'agenda' : title.startsWith('Notes') ? 'grades' : title.startsWith('Documents') ? 'documents' : 'projects'
  return `
    <a class="portal-tile" href="#${route}">
      <span>${escapeHtml(title)}</span>
      <strong>${escapeHtml(detail)}</strong>
      <small class="muted">${escapeHtml(meta)}</small>
    </a>
  `
}

function renderList(items, renderer) {
  return items.length ? items.map(renderer).join('') : emptyState()
}

function emptyState() {
  return '<p class="muted">Aucune donnee renvoyee par MyGES pour cette rubrique.</p>'
}

function unwrap(payload) {
  return payload?.result ?? payload ?? []
}

function toArray(value) {
  if (Array.isArray(value)) return value
  if (value && typeof value === 'object') return Object.values(value).flat().filter(item => item && typeof item === 'object')
  return []
}

function profileFromApi(payload) {
  const result = unwrap(payload)
  const firstName = valueOf(result, ['first_name', 'firstname', 'firstName', 'prenom'])
  const lastName = valueOf(result, ['last_name', 'lastname', 'lastName', 'nom'])
  const display = valueOf(result, ['displayName', 'display_name', 'full_name', 'name'])
  return {
    name: display || [firstName, lastName].filter(Boolean).join(' ') || 'Profil MyGES',
    school: valueOf(result, ['school', 'school_name', 'campus']) || 'MyGES',
    program: valueOf(result, ['program', 'formation', 'class', 'class_name', 'promotion']) || 'Espace etudiant'
  }
}

function updateProfileHeader() {
  profileName.textContent = data.profile?.name || 'Profil MyGES'
  profileInitials.textContent = initials(profileName.textContent)
}

function initials(name) {
  const parts = name.split(/\s+/).filter(Boolean)
  return ((parts[0]?.[0] || 'M') + (parts[1]?.[0] || 'G')).toUpperCase()
}

function firstName(name) {
  return name.split(/\s+/).filter(Boolean)[0] || 'toi'
}

function sortedAgenda() {
  return data.agenda.slice().sort((a, b) => Number(eventStart(a) || 0) - Number(eventStart(b) || 0))
}

function eventStart(event) {
  return valueOf(event, ['start', 'starts_at', 'start_date', 'date_start', 'begin'])
}

function eventEnd(event) {
  return valueOf(event, ['end', 'ends_at', 'end_date', 'date_end', 'finish'])
}

function eventTitle(event) {
  return valueOf(event, ['title', 'name', 'course_name', 'discipline', 'course']) || 'Cours'
}

function eventRoom(event) {
  return valueOf(event, ['room', 'classroom', 'location', 'campus'])
}

function eventType(event) {
  return valueOf(event, ['type', 'category', 'kind']) || 'MyGES'
}

function gradeTitle(grade) {
  return valueOf(grade, ['course', 'course_name', 'name', 'code']) || 'Matiere'
}

function gradePeriod(grade) {
  return valueOf(grade, ['trimester_name', 'period', 'year']) || 'Sans periode'
}

function gradeEcts(grade) {
  return Number(valueOf(grade, ['ects']) || 0)
}

function teacherName(item) {
  const first = valueOf(item, ['teacher_first_name', 'teacherFirstName'])
  const last = valueOf(item, ['teacher_last_name', 'teacherLastName', 'teacher'])
  return [first, last].filter(Boolean).join(' ') || '-'
}

function absenceStatus(absence) {
  const justified = valueOf(absence, ['justified', 'is_justified', 'justified_status'])
  if (justified === true || justified === 'true' || justified === 'Oui') return 'Oui'
  if (justified === false || justified === 'false' || justified === 'Non') return 'Non'
  return valueOf(absence, ['status']) || 'MyGES'
}

function projectTitle(project) {
  return valueOf(project, ['name', 'title', 'project_name']) || 'Projet'
}

function documentType(document) {
  return valueOf(document, ['type', 'category', 'kind']) || 'Document'
}

function yearValue(year) {
  return String(valueOf(year, ['year', 'name', 'label', 'school_year']) || year || '').trim()
}

function valueOf(source, keys) {
  for (const key of keys) {
    const value = source?.[key]
    if (value == null) continue
    if (typeof value === 'object') {
      const nested = value.name || value.label || value.title || value.value
      if (nested != null && String(nested).trim()) return nested
      continue
    }
    if (String(value).trim()) return value
  }
  return ''
}

function unique(values) {
  return [...new Set(values.filter(Boolean).map(String))]
}

function sum(values) {
  return values.reduce((total, value) => total + value, 0)
}

function formatDateTime(value) {
  if (!value) return ''
  const date = toDate(value)
  if (!date) return String(value)
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'short' }).format(date)
}

function formatTime(value) {
  if (!value) return '--:--'
  const date = toDate(value)
  if (!date) return String(value)
  return new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' }).format(date)
}

function toDate(value) {
  if (value instanceof Date) return value
  if (typeof value === 'number') return new Date(value < 10000000000 ? value * 1000 : value)
  const parsed = Date.parse(value)
  return Number.isNaN(parsed) ? null : new Date(parsed)
}

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}
