const CONFIG = {
  apiBaseUrl: 'https://api.kordis.fr/',
  authorizeUrl: 'https://authentication.kordis.fr/oauth/authorize',
  clientId: 'skolae-app',
  storageKey: 'studly.pwa.session',
  cacheKey: 'studly.pwa.snapshot'
}

const state = {
  route: 'dashboard',
  loading: false,
  error: '',
  session: readJson(CONFIG.storageKey),
  snapshot: readJson(CONFIG.cacheKey) || emptySnapshot()
}

const titleByRoute = {
  dashboard: 'Tableau de bord',
  agenda: 'Agenda',
  grades: 'Notes',
  absences: 'Absences',
  projects: 'Projets',
  news: 'Actualites'
}

const content = document.querySelector('#content')
const screenTitle = document.querySelector('#screen-title')
const syncButton = document.querySelector('#sync-button')

document.querySelectorAll('[data-route]').forEach(button => {
  button.addEventListener('click', () => {
    state.route = button.dataset.route
    render()
  })
})

syncButton.addEventListener('click', () => refresh())

window.addEventListener('hashchange', consumeOAuthHash)

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('sw.js')
}

consumeOAuthHash()
render()
if (state.session?.accessToken) refresh()

function render() {
  screenTitle.textContent = titleByRoute[state.route]
  document.querySelectorAll('[data-route]').forEach(button => {
    button.classList.toggle('is-active', button.dataset.route === state.route)
  })

  if (!state.session?.accessToken) {
    renderLogin()
    return
  }

  const views = {
    dashboard: renderDashboard,
    agenda: renderAgenda,
    grades: renderGrades,
    absences: renderAbsences,
    projects: renderProjects,
    news: renderNews
  }
  views[state.route]()
}

function renderLogin() {
  content.innerHTML = `
    <section class="hero">
      <h2>Studly sur iPhone</h2>
      <p>Connecte-toi avec Kordis, puis ajoute cette page a l'ecran d'accueil depuis Safari.</p>
      <div class="actions">
        <button class="button" id="login-button" type="button">Connexion Kordis</button>
      </div>
    </section>
    <section class="card grid">
      <div class="field">
        <label for="redirect-uri">URL de retour OAuth</label>
        <input id="redirect-uri" autocomplete="url" inputmode="url" placeholder="${escapeHtml(location.href.split('#')[0])}" value="${escapeHtml(configuredRedirectUri())}">
      </div>
      <div class="field">
        <label for="token-input">Jeton d'acces</label>
        <input id="token-input" autocomplete="off" inputmode="text" placeholder="Bearer ou access_token">
      </div>
      <div class="actions">
        <button class="ghost-button" id="save-redirect" type="button">Enregistrer URL</button>
        <button class="ghost-button" id="save-token" type="button">Utiliser le jeton</button>
      </div>
      ${state.error ? `<p class="error">${escapeHtml(state.error)}</p>` : ''}
      <p class="muted">L'URL de retour doit etre autorisee cote Kordis. Laisser vide evite le 403 localhost, mais le retour automatique vers la PWA necessite une URL web whitelistee.</p>
    </section>
  `
  document.querySelector('#login-button').addEventListener('click', startOAuth)
  document.querySelector('#save-redirect').addEventListener('click', () => {
    const value = document.querySelector('#redirect-uri').value.trim()
    if (value) localStorage.setItem('studly.pwa.redirectUri', value)
    else localStorage.removeItem('studly.pwa.redirectUri')
    startOAuth()
  })
  document.querySelector('#save-token').addEventListener('click', () => {
    const value = document.querySelector('#token-input').value.trim().replace(/^Bearer\s+/i, '')
    if (!value) return
    saveSession({ accessToken: value, expiresAt: null })
    refresh()
  })
}

function renderDashboard() {
  const { profile, agenda, grades, absences, projects, news, updatedAt } = state.snapshot
  const nextCourse = agenda
    .filter(item => toTime(pick(item, ['start', 'startsAt', 'dateStart', 'beginAt'])) > Date.now())
    .sort((a, b) => toTime(pick(a, ['start', 'startsAt', 'dateStart', 'beginAt'])) - toTime(pick(b, ['start', 'startsAt', 'dateStart', 'beginAt'])))[0]
  const average = computeAverage(grades)
  const lateProjects = projects
    .filter(item => toTime(pick(item, ['deadline', 'dueAt', 'endDate', 'dateLimit'])) > Date.now())
    .sort((a, b) => toTime(pick(a, ['deadline', 'dueAt', 'endDate', 'dateLimit'])) - toTime(pick(b, ['deadline', 'dueAt', 'endDate', 'dateLimit'])))

  content.innerHTML = `
    ${installHint()}
    ${state.error ? `<section class="card"><p class="error">${escapeHtml(state.error)}</p></section>` : ''}
    <section class="hero">
      <h2>${escapeHtml(displayProfileName(profile))}</h2>
      <p>${escapeHtml(pick(profile, ['school', 'campus', 'program', 'promotion']) || 'Donnees MyGES synchronisees')}</p>
      <p class="muted">${updatedAt ? `Derniere sync ${formatDateTime(updatedAt)}` : 'Aucune synchronisation locale'}</p>
    </section>
    <section class="grid cols-2">
      ${metricCard('Prochain cours', nextCourse ? displayTitle(nextCourse) : 'Aucun', nextCourse ? formatDateTime(pick(nextCourse, ['start', 'startsAt', 'dateStart', 'beginAt'])) : 'Agenda a jour')}
      ${metricCard('Moyenne', average ?? '-', `${grades.length} notes`)}
      ${metricCard('Absences', String(absences.length), 'Total synchronise')}
      ${metricCard('Prochaine echeance', lateProjects[0] ? displayTitle(lateProjects[0]) : 'Aucune', lateProjects[0] ? formatDateTime(pick(lateProjects[0], ['deadline', 'dueAt', 'endDate', 'dateLimit'])) : 'Projets a jour')}
    </section>
    ${listSection('Dernieres actualites', news.slice(0, 3))}
  `
}

function renderAgenda() {
  const items = state.snapshot.agenda
    .slice()
    .sort((a, b) => toTime(pick(a, ['start', 'startsAt', 'dateStart', 'beginAt'])) - toTime(pick(b, ['start', 'startsAt', 'dateStart', 'beginAt'])))
  content.innerHTML = listSection('Agenda', items, item => formatDateTime(pick(item, ['start', 'startsAt', 'dateStart', 'beginAt'])))
}

function renderGrades() {
  const items = state.snapshot.grades
    .slice()
    .sort((a, b) => toTime(pick(b, ['date', 'gradedAt', 'publishedAt'])) - toTime(pick(a, ['date', 'gradedAt', 'publishedAt'])))
  content.innerHTML = `
    <section class="grid cols-2">
      ${metricCard('Moyenne', computeAverage(items) ?? '-', `${items.length} notes`)}
    </section>
    ${listSection('Notes', items, item => {
      const value = pick(item, ['value', 'grade', 'mark', 'score'])
      const max = pick(item, ['max', 'outOf', 'scale'])
      return value ? `${value}${max ? `/${max}` : '/20'}` : ''
    })}
  `
}

function renderAbsences() {
  content.innerHTML = listSection('Absences', state.snapshot.absences, item => pick(item, ['justified', 'status']) || formatDateTime(pick(item, ['date', 'startsAt'])))
}

function renderProjects() {
  content.innerHTML = listSection('Projets', state.snapshot.projects, item => formatDateTime(pick(item, ['deadline', 'dueAt', 'endDate', 'dateLimit'])))
}

function renderNews() {
  content.innerHTML = listSection('Actualites', state.snapshot.news, item => formatDateTime(pick(item, ['date', 'publishedAt', 'createdAt'])))
}

async function refresh() {
  if (!state.session?.accessToken || state.loading) return
  state.loading = true
  syncButton.setAttribute('aria-busy', 'true')
  try {
    const profile = await api('me/profile')
    const years = await resolveYears()
    const now = Date.now()
    const agenda = await api(`me/agenda?start=${Math.floor((now - 7 * 86400000) / 1000)}&end=${Math.floor((now + 45 * 86400000) / 1000)}`)
    const [grades, absences, projects, news] = await Promise.all([
      collectYears(years, year => api(`me/${encodeURIComponent(year)}/grades`)),
      collectYears(years, year => api(`me/${encodeURIComponent(year)}/absences`)),
      collectYears(years, year => api(`me/${encodeURIComponent(year)}/projects`)),
      api('me/news').then(normalizeList)
    ])
    state.snapshot = {
      profile,
      agenda: normalizeList(agenda),
      grades,
      absences,
      projects,
      news,
      updatedAt: new Date().toISOString()
    }
    state.error = ''
    localStorage.setItem(CONFIG.cacheKey, JSON.stringify(state.snapshot))
  } catch (error) {
    state.error = readableError(error)
  } finally {
    state.loading = false
    syncButton.removeAttribute('aria-busy')
    render()
  }
}

async function resolveYears() {
  try {
    const data = normalizeList(await api('me/years'))
    const years = data.map(item => String(pick(item, ['year', 'id', 'name']) || item)).filter(Boolean)
    return years.length ? years : [schoolYear()]
  } catch {
    return [schoolYear()]
  }
}

async function collectYears(years, loader) {
  const settled = await Promise.allSettled(years.map(year => loader(year).then(normalizeList)))
  return settled.flatMap(result => result.status === 'fulfilled' ? result.value : [])
}

async function api(path) {
  const response = await fetch(new URL(path, CONFIG.apiBaseUrl), {
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${state.session.accessToken}`
    }
  })
  if (response.status === 401) {
    logout()
    throw new Error('Session expiree')
  }
  if (!response.ok) throw new Error(`API ${response.status}`)
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

function startOAuth() {
  const url = new URL(CONFIG.authorizeUrl)
  url.searchParams.set('response_type', 'token')
  url.searchParams.set('client_id', CONFIG.clientId)
  const configured = configuredRedirectUri()
  if (configured) url.searchParams.set('redirect_uri', configured)
  location.href = url.toString()
}

function consumeOAuthHash() {
  const hash = new URLSearchParams(location.hash.replace(/^#/, ''))
  const token = hash.get('access_token')
  if (!token) return
  const expiresIn = Number(hash.get('expires_in') || 0)
  saveSession({
    accessToken: token,
    expiresAt: expiresIn ? new Date(Date.now() + expiresIn * 1000).toISOString() : null
  })
  history.replaceState(null, '', location.pathname + location.search)
  refresh()
}

function saveSession(session) {
  state.session = session
  localStorage.setItem(CONFIG.storageKey, JSON.stringify(session))
}

function logout() {
  state.session = null
  localStorage.removeItem(CONFIG.storageKey)
}

function redirectUri() {
  return configuredRedirectUri() || location.href.split('#')[0]
}

function configuredRedirectUri() {
  const value = localStorage.getItem('studly.pwa.redirectUri') || ''
  return isLocalRedirectUri(value) ? '' : value
}

function isLocalRedirectUri(value) {
  try {
    const url = new URL(value)
    return ['localhost', '127.0.0.1', '::1'].includes(url.hostname)
  } catch {
    return false
  }
}

function readJson(key) {
  try {
    return JSON.parse(localStorage.getItem(key))
  } catch {
    return null
  }
}

function emptySnapshot() {
  return { profile: {}, agenda: [], grades: [], absences: [], projects: [], news: [], updatedAt: null }
}

function normalizeList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  for (const key of ['data', 'items', 'results', 'response']) {
    if (Array.isArray(data[key])) return data[key]
  }
  return Object.values(data).find(Array.isArray) || []
}

function pick(object, keys) {
  if (!object || typeof object !== 'object') return ''
  for (const key of keys) {
    if (object[key] !== undefined && object[key] !== null && object[key] !== '') return object[key]
  }
  return ''
}

function displayProfileName(profile) {
  const first = pick(profile, ['firstname', 'firstName', 'prenom'])
  const last = pick(profile, ['lastname', 'lastName', 'nom'])
  return [first, last].filter(Boolean).join(' ') || pick(profile, ['name', 'displayName', 'email']) || 'Bonjour'
}

function displayTitle(item) {
  return pick(item, ['title', 'name', 'label', 'course', 'module', 'subject']) || 'Sans titre'
}

function toTime(value) {
  if (!value) return 0
  if (typeof value === 'number') return value < 10000000000 ? value * 1000 : value
  const parsed = Date.parse(value)
  return Number.isNaN(parsed) ? 0 : parsed
}

function formatDateTime(value) {
  const time = toTime(value)
  if (!time) return ''
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(time))
}

function computeAverage(grades) {
  const values = grades
    .map(item => Number(String(pick(item, ['value', 'grade', 'mark', 'score'])).replace(',', '.')))
    .filter(value => Number.isFinite(value))
  if (!values.length) return null
  return (values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(2).replace('.', ',')
}

function listSection(title, items, meta = item => formatDateTime(pick(item, ['date', 'publishedAt', 'createdAt']))) {
  const body = items.length
    ? items.map(item => `
      <article class="list-item">
        <strong>${escapeHtml(displayTitle(item))}</strong>
        <span class="muted">${escapeHtml(meta(item) || pick(item, ['description', 'body', 'room', 'teacher']) || '')}</span>
      </article>
    `).join('')
    : '<p class="muted">Aucune donnee synchronisee.</p>'
  return `<section class="card"><h2>${escapeHtml(title)}</h2><div class="list">${body}</div></section>`
}

function metricCard(label, value, detail) {
  return `
    <article class="card metric">
      <span class="muted">${escapeHtml(label)}</span>
      <strong>${escapeHtml(value)}</strong>
      <span class="muted">${escapeHtml(detail || '')}</span>
    </article>
  `
}

function installHint() {
  const standalone = window.navigator.standalone || matchMedia('(display-mode: standalone)').matches
  if (standalone) return ''
  return '<section class="card install-hint"><strong>Installation iOS</strong><p class="muted">Dans Safari : Partager, puis Ajouter a l\\u0027ecran d\\u0027accueil.</p></section>'
}

function readableError(error) {
  if (!navigator.onLine) return 'Hors ligne : affichage du dernier cache local.'
  return error?.message || 'Synchronisation impossible.'
}

function schoolYear() {
  const date = new Date()
  const year = date.getFullYear()
  return date.getMonth() >= 7 ? `${year}-${year + 1}` : `${year - 1}-${year}`
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[char]))
}
