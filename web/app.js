import {
  parseProfile, parseYears, parseAgenda, parseGrades, parseAbsences, parseProjects, parseNews,
  toGradeSummary, mainGrades, withSimulatedValues, withRecomputedMainGrades, gradeComponents, gradeBlockKey,
  parseCourses, parseCourseSyllabus, parseDocuments, parseDirectory, parsePracticals, parseEvents
} from './parse.js'

const CONFIG = {
  apiBaseUrl: 'https://api.kordis.fr/',
  authorizeUrl: 'https://authentication.kordis.fr/oauth/authorize',
  clientId: 'skolae-app',
  storageKey: 'studly.pwa.session',
  cacheKey: 'studly.pwa.snapshot'
}

resetLocalSessionIfRequested()

const state = {
  route: 'dashboard',
  loading: false,
  error: '',
  session: readJson(CONFIG.storageKey),
  snapshot: readJson(CONFIG.cacheKey) || emptySnapshot(),
  agendaMode: 'week',
  agendaDate: startOfDay(Date.now()),
  gradesPeriod: null,
  sim: false,
  simValues: readJson('studly.pwa.sim') || {},
  blocks: readJson('studly.pwa.blocks') || {}
}

const titleByRoute = {
  dashboard: 'Tableau de bord',
  agenda: 'Agenda',
  grades: 'Notes',
  absences: 'Absences',
  projects: 'Projets',
  news: 'Actualites',
  more: 'Plus',
  courses: 'Cours',
  practicals: 'TP',
  documents: 'Documents',
  directory: 'Annuaire',
  events: 'Evenements'
}
const SUB_ROUTES = ['courses', 'practicals', 'documents', 'directory', 'events']

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
  const navRoute = SUB_ROUTES.includes(state.route) ? 'more' : state.route
  document.querySelectorAll('[data-route]').forEach(button => {
    button.classList.toggle('is-active', button.dataset.route === navRoute)
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
    news: renderNews,
    more: renderMore,
    courses: renderCourses,
    practicals: renderPracticals,
    documents: renderDocuments,
    directory: renderDirectory,
    events: renderEvents
  }
  views[state.route]()
}

function renderMore() {
  const links = [
    ['courses', 'Cours', 'Matieres, enseignants, syllabus, fichiers'],
    ['practicals', 'TP / Practicals', 'Travaux pratiques et groupes'],
    ['documents', 'Documents', 'Documents annuels et de scolarite'],
    ['directory', 'Annuaire', 'Etudiants et enseignants'],
    ['events', 'Evenements', 'Evenements etudiants']
  ]
  content.innerHTML = `<section class="card"><div class="list">${links.map(([r, t, d]) =>
    `<article class="list-item is-clickable" data-goto="${r}"><strong>${t}</strong><span class="muted">${d}</span></article>`).join('')}</div></section>`
  content.querySelectorAll('[data-goto]').forEach(b => b.addEventListener('click', () => { state.route = b.dataset.goto; render() }))
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
        <label for="token-input">Jeton d'acces ou URL callback</label>
        <input id="token-input" autocomplete="off" inputmode="text" placeholder="Bearer, access_token ou comreseaugesskolae:/oauth2redirect#...">
      </div>
      <div class="actions">
        <button class="ghost-button" id="save-redirect" type="button">Enregistrer URL</button>
        <button class="ghost-button" id="save-token" type="button">Utiliser le jeton</button>
      </div>
      ${state.error ? `<p class="error">${escapeHtml(state.error)}</p>` : ''}
      <p class="muted">Sans URL web whitelistee, Kordis renvoie vers comreseaugesskolae:/oauth2redirect. Colle l'URL callback complete ici pour extraire le token.</p>
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
    const parsed = parseTokenInput(document.querySelector('#token-input').value)
    if (!parsed.accessToken) return
    saveSession(parsed)
    refresh()
  })
}

function renderDashboard() {
  const { profile, agenda, grades, absences, projects, news, updatedAt } = state.snapshot
  const now = Date.now()

  const nextEvent = agenda.filter(e => e.endsAt > now).sort((a, b) => a.startsAt - b.startsAt)[0]

  // Moyenne de la période courante (comme le widget Android) sur les notes principales.
  const period = currentPeriod(grades)
  const periodGrades = period ? grades.filter(g => g.period === period) : grades
  const summary = toGradeSummary(mainGrades(periodGrades))
  const average = summary.weightedAverage != null ? summary.weightedAverage.toFixed(2).replace('.', ',') : '-'

  const periodAbs = latestPeriodAbsences(absences)
  const unjustified = periodAbs.filter(a => !a.justified).length

  const dueProject = projects
    .map(p => ({ p, at: nextDeadline(p, now) }))
    .filter(x => x.at != null)
    .sort((a, b) => a.at - b.at)[0]

  const latestGrades = mainGrades(grades).filter(g => g.value != null)
    .sort((a, b) => (b.date || 0) - (a.date || 0) || (a.id < b.id ? 1 : -1)).slice(0, 3)

  content.innerHTML = `
    ${installHint()}
    ${state.error ? `<section class="card"><p class="error">${escapeHtml(state.error)}</p></section>` : ''}
    <section class="hero">
      <h2>${escapeHtml(profile.displayName || 'Bonjour')}</h2>
      <p>${escapeHtml([profile.program, profile.school].filter(Boolean).join(' · ') || 'Donnees MyGES synchronisees')}</p>
      <p class="muted">${updatedAt ? `Derniere sync ${formatDateTime(updatedAt)}` : 'Aucune synchronisation locale'}</p>
    </section>
    <section class="grid cols-2">
      ${metricCard('Prochain cours', nextEvent ? nextEvent.title : 'Aucun',
        nextEvent ? `${formatDateTime(nextEvent.startsAt)}${nextEvent.room ? ' · ' + nextEvent.room : ''}` : 'Agenda a jour')}
      ${metricCard('Moyenne', average, period ? `${summary.gradedCount} notes · ${period}` : `${summary.gradedCount} notes`)}
      ${metricCard('Absences non justifiees', String(unjustified), `${periodAbs.length} au total`)}
      ${metricCard('Prochaine echeance', dueProject ? dueProject.p.name : 'Aucune',
        dueProject ? formatDateTime(dueProject.at) : 'Projets a jour')}
    </section>
    ${gradesSection('Dernieres notes', latestGrades)}
    ${newsSection(news.slice(0, 3))}
  `
}

function renderAgenda() {
  const events = state.snapshot.agenda.slice().sort((a, b) => a.startsAt - b.startsAt)
  const mode = state.agendaMode
  const modes = [['grid', 'Grille'], ['week', 'Semaine'], ['day', 'Jour'], ['month', 'Mois'], ['list', 'Liste']]

  let nav = ''
  if (mode !== 'list') {
    let label
    if (mode === 'day') label = formatDate(state.agendaDate)
    else if (mode === 'month') label = new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(new Date(state.agendaDate))
    else label = `${formatDate(mondayOf(state.agendaDate))} – ${formatDate(mondayOf(state.agendaDate) + 5 * DAY)}`
    nav = `<div class="agenda-nav">
      <button class="icon-btn" data-nav="-1" aria-label="Precedent">‹</button>
      <strong>${escapeHtml(label)}</strong>
      <button class="icon-btn" data-nav="1" aria-label="Suivant">›</button>
    </div>`
  }

  let bodyHtml
  let bare = false
  if (mode === 'list') {
    bodyHtml = `<section class="card"><div class="list">${agendaDayList(events)}</div></section>`
    bare = true
  } else if (mode === 'day') {
    bodyHtml = `<section class="card"><div class="list">${agendaDayGroup(events.filter(e => sameDay(e.startsAt, state.agendaDate)), state.agendaDate)}</div></section>`
    bare = true
  } else if (mode === 'week') {
    const start = mondayOf(state.agendaDate)
    const days = Array.from({ length: 7 }, (_, i) => start + i * DAY)
    const chunks = days.map(d => agendaDayGroup(events.filter(e => sameDay(e.startsAt, d)), d, true)).filter(Boolean)
    bodyHtml = `<section class="card"><div class="list">${chunks.length ? chunks.join('') : '<p class="muted">Aucun cours cette semaine.</p>'}</div></section>`
    bare = true
  } else if (mode === 'grid') {
    bodyHtml = agendaGrid(mondayOf(state.agendaDate), events)
  } else {
    const dayEvents = events.filter(e => sameDay(e.startsAt, state.agendaDate))
    bodyHtml = agendaMonth(state.agendaDate, events) +
      `<section class="card"><div class="list">${agendaDayGroup(dayEvents, state.agendaDate)}</div></section>`
  }

  content.innerHTML = `
    <div class="chip-row">${modes.map(([m, l]) =>
      `<button class="chip${m === mode ? ' is-active' : ''}" data-mode="${m}">${l}</button>`).join('')}</div>
    ${nav}
    <button class="ghost-button" data-ics style="margin-bottom:10px">Exporter (.ics)</button>
    ${bodyHtml}
  `

  content.querySelectorAll('[data-mode]').forEach(b => b.addEventListener('click', () => { state.agendaMode = b.dataset.mode; renderAgenda() }))
  content.querySelectorAll('[data-nav]').forEach(b => b.addEventListener('click', () => {
    let step = Number(b.dataset.nav)
    if (mode === 'day') state.agendaDate = startOfDay(state.agendaDate + step * DAY)
    else if (mode === 'month') { const d = new Date(state.agendaDate); d.setMonth(d.getMonth() + step); state.agendaDate = startOfDay(d.getTime()) }
    else state.agendaDate = startOfDay(state.agendaDate + step * 7 * DAY)
    renderAgenda()
  }))
  content.querySelectorAll('[data-day]').forEach(b => b.addEventListener('click', () => {
    state.agendaDate = Number(b.dataset.day)
    renderAgenda()
  }))
  content.querySelectorAll('[data-event]').forEach(b => b.addEventListener('click', () => {
    const ev = events.find(e => e.id === b.dataset.event)
    if (ev) openEventModal(ev)
  }))
  const ics = content.querySelector('[data-ics]')
  if (ics) ics.addEventListener('click', () => downloadIcs(events))
}

// Hour-by-hour week grid (port of AgendaWeekGrid + layoutDayEvents).
const HOUR_PX = 48
const EVENT_PALETTE = ['#039BE5', '#0B8043', '#D50000', '#8E24AA', '#F4511E', '#33B679', '#3F51B5', '#7986CB', '#F6BF26', '#E67C73', '#616161']

function hashCode(s) { let h = 0; for (let i = 0; i < s.length; i++) h = (Math.imul(31, h) + s.charCodeAt(i)) | 0; return h }
function eventColor(e) { return EVENT_PALETTE[(hashCode(e.courseId || e.title || e.id) & 0x7fffffff) % EVENT_PALETTE.length] }

function layoutDayEvents(events) {
  const items = events.map(ev => {
    const s = new Date(ev.startsAt), e = new Date(ev.endsAt)
    const startMin = s.getHours() * 60 + s.getMinutes()
    let endMin = e.getHours() * 60 + e.getMinutes()
    if (startOfDay(ev.endsAt) !== startOfDay(ev.startsAt)) endMin = 24 * 60
    if (endMin <= startMin) endMin = startMin + 30
    return { ev, startMin, endMin }
  }).sort((a, b) => a.startMin - b.startMin)

  const result = []
  let i = 0
  while (i < items.length) {
    let clusterEnd = items[i].endMin
    let j = i + 1
    while (j < items.length && items[j].startMin < clusterEnd) { clusterEnd = Math.max(clusterEnd, items[j].endMin); j++ }
    const cluster = items.slice(i, j)
    const laneEnds = []
    cluster.forEach(it => {
      let lane = laneEnds.findIndex(end => end <= it.startMin)
      if (lane === -1) { laneEnds.push(it.endMin); lane = laneEnds.length - 1 } else laneEnds[lane] = it.endMin
      it.lane = lane
    })
    cluster.forEach(it => result.push({ ...it, laneCount: laneEnds.length }))
    i = j
  }
  return result
}

function agendaGrid(weekStart, allEvents) {
  const days = Array.from({ length: 6 }, (_, i) => weekStart + i * DAY)
  const byDay = days.map(d => allEvents.filter(e => sameDay(e.startsAt, d)))
  const flat = byDay.flat()
  const startHour = Math.min(8, ...flat.map(e => new Date(e.startsAt).getHours()).concat([8]))
  const endHour = Math.min(24, Math.max(19, ...flat.map(e => { const d = new Date(e.endsAt); return d.getMinutes() > 0 ? d.getHours() + 1 : d.getHours() })))
  const H = (endHour - startHour) * HOUR_PX
  const today = startOfDay(Date.now())

  const gutter = `<div class="gcol gutter"><div class="ghead"></div><div class="gbody" style="height:${H}px">${
    Array.from({ length: endHour - startHour }, (_, k) => `<div class="ghour" style="top:${k * HOUR_PX}px">${startHour + k}h</div>`).join('')
  }</div></div>`

  const cols = days.map((d, di) => {
    const lines = Array.from({ length: endHour - startHour }, (_, k) => `<div class="gline" style="top:${k * HOUR_PX}px"></div>`).join('')
    const blocks = layoutDayEvents(byDay[di]).map(le => {
      const top = (le.startMin - startHour * 60) / 60 * HOUR_PX
      const h = Math.max(22, (le.endMin - le.startMin) / 60 * HOUR_PX)
      const w = 100 / le.laneCount
      return `<div class="gevent" data-event="${escapeHtml(le.ev.id)}" style="top:${top}px;height:${h}px;left:${le.lane * w}%;width:${w}%;background:${eventColor(le.ev)}">
        <span class="ge-title">${escapeHtml(le.ev.title || 'Cours')}</span>
        <span class="ge-sub">${formatTime(le.ev.startsAt)}${le.ev.room ? ' · ' + escapeHtml(le.ev.room) : ''}</span>
      </div>`
    }).join('')
    const head = new Intl.DateTimeFormat('fr-FR', { weekday: 'short', day: 'numeric' }).format(new Date(d))
    return `<div class="gcol"><div class="ghead${d === today ? ' is-today' : ''}">${escapeHtml(head)}</div><div class="gbody" style="height:${H}px">${lines}${blocks}</div></div>`
  }).join('')

  return `<section class="card grid-scroll"><div class="grid-cols">${gutter}${cols}</div></section>`
}

function agendaMonth(refDate, allEvents) {
  const ref = new Date(refDate)
  const year = ref.getFullYear(), month = ref.getMonth()
  const first = new Date(year, month, 1)
  const len = new Date(year, month + 1, 0).getDate()
  const lead = (first.getDay() + 6) % 7 // Monday-first offset
  const daysWithEvents = new Set(allEvents.map(e => startOfDay(e.startsAt)))
  const selected = startOfDay(refDate)
  const heads = ['L', 'M', 'M', 'J', 'V', 'S', 'D'].map(d => `<span class="mo-head">${d}</span>`).join('')
  const blanks = Array.from({ length: lead }, () => '<span></span>').join('')
  const cells = Array.from({ length: len }, (_, k) => {
    const day = k + 1
    const ms = startOfDay(new Date(year, month, day).getTime())
    const cls = ['mo-cell', ms === selected ? 'is-selected' : '', daysWithEvents.has(ms) ? 'has-event' : ''].filter(Boolean).join(' ')
    return `<button class="${cls}" data-day="${ms}">${day}</button>`
  }).join('')
  return `<section class="card"><div class="month-grid">${heads}${blanks}${cells}</div></section>`
}

function downloadIcs(events) {
  const blob = new Blob([toIcs(events)], { type: 'text/calendar' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'agenda.ics'
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

function toIcs(events) {
  const pad = n => String(n).padStart(2, '0')
  const fmt = ms => { const d = new Date(ms); return `${d.getUTCFullYear()}${pad(d.getUTCMonth() + 1)}${pad(d.getUTCDate())}T${pad(d.getUTCHours())}${pad(d.getUTCMinutes())}${pad(d.getUTCSeconds())}Z` }
  const esc = s => String(s || '').replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\r?\n/g, '\\n')
  const fold = line => line.length <= 75 ? line : line.match(/.{1,75}/g).join('\r\n ')
  const lines = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//MyGES PWA//Calendar Export//EN', 'CALSCALE:GREGORIAN']
  for (const e of events) {
    const desc = [e.teacher, e.type, e.modality].filter(Boolean).join(' - ')
    lines.push('BEGIN:VEVENT', `UID:${esc(e.id)}`, `DTSTART:${fmt(e.startsAt)}`, `DTEND:${fmt(e.endsAt)}`,
      `SUMMARY:${esc(e.title)}`, `DESCRIPTION:${esc(desc)}`, `LOCATION:${esc(e.room)}`, 'END:VEVENT')
  }
  lines.push('END:VCALENDAR')
  return lines.map(fold).join('\r\n') + '\r\n'
}

function agendaDayList(events) {
  if (!events.length) return '<p class="muted">Aucune donnee synchronisee.</p>'
  // Group consecutive events by day with a header, like the Android 7-day view.
  let out = ''
  let lastDay = null
  for (const e of events) {
    const day = startOfDay(e.startsAt)
    if (day !== lastDay) { out += `<h3 class="day-head">${escapeHtml(formatDate(day))}</h3>`; lastDay = day }
    out += eventRow(e)
  }
  return out
}

function agendaDayGroup(events, day, withHeader = false) {
  if (!events.length) return withHeader ? '' : '<p class="muted">Aucun cours ce jour.</p>'
  const header = withHeader ? `<h3 class="day-head">${escapeHtml(formatDate(day))}</h3>` : ''
  return header + events.sort((a, b) => a.startsAt - b.startsAt).map(eventRow).join('')
}

function eventRow(e) {
  const meta = [`${formatTime(e.startsAt)}–${formatTime(e.endsAt)}`, e.room, e.teacher].filter(Boolean).join(' · ')
  return `<article class="list-item is-clickable" data-event="${escapeHtml(e.id)}">
    <strong>${escapeHtml(e.title || 'Cours')}</strong>
    <span class="muted">${escapeHtml(meta)}</span>
  </article>`
}

function openEventModal(e) {
  const rows = [
    ['Debut', formatDateTime(e.startsAt)],
    ['Fin', formatDateTime(e.endsAt)],
    ['Salle', e.room],
    ['Adresse', e.address, e.address ? `https://maps.google.com/?q=${encodeURIComponent(e.address)}` : null],
    ['Enseignant', e.teacher],
    ['Modalite', e.modality],
    ['Type', e.type]
  ].filter(([, v]) => v).map(([k, v, href]) =>
    `<div class="kv"><span class="muted">${k}</span>${href
      ? `<a href="${escapeHtml(href)}" target="_blank" rel="noopener">${escapeHtml(v)}</a>`
      : `<span>${escapeHtml(v)}</span>`}</div>`).join('')
  openModal(e.title || 'Cours', rows)
}

// ---- shared detail helpers ----
function kvRows(pairs) {
  return pairs.filter(([, v]) => v != null && v !== '').map(([k, v, href]) =>
    `<div class="kv"><span class="muted">${escapeHtml(k)}</span>${href
      ? `<a href="${escapeHtml(href)}" target="_blank" rel="noopener">${escapeHtml(String(v))}</a>`
      : `<span>${escapeHtml(String(v))}</span>`}</div>`).join('')
}

// Strip active content from CMS HTML before injecting it.
function sanitizeHtml(html) {
  return String(html)
    .replace(/<\s*(script|style|iframe|object|embed|link|meta)[^>]*>[\s\S]*?<\s*\/\s*\1\s*>/gi, '')
    .replace(/<\s*(script|style|iframe|object|embed|link|meta)[^>]*\/?>/gi, '')
    .replace(/\son\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    .replace(/(href|src)\s*=\s*("javascript:[^"]*"|'javascript:[^']*')/gi, '$1="#"')
}

function docRow(d) {
  return `<div class="kv"><span>${escapeHtml(d.title || d.fileName || 'Document')}</span>
    <button class="chip" data-doc="${escapeHtml(d.downloadUrl || '')}" data-name="${escapeHtml(d.fileName || d.title || 'document')}">Telecharger</button></div>`
}
function wireDocDownloads() {
  modalEl.querySelectorAll('[data-doc]').forEach(b => {
    if (b.dataset.wired) return
    b.dataset.wired = '1'
    b.addEventListener('click', () => downloadDocument(b.dataset.doc, b.dataset.name, b))
  })
}
async function downloadDocument(url, name, btn) {
  if (!url) return
  const label = btn ? btn.textContent : ''
  if (btn) { btn.textContent = '…'; btn.disabled = true }
  try {
    const res = await fetch(new URL(url, CONFIG.apiBaseUrl), { headers: { Authorization: `Bearer ${state.session.accessToken}`, Accept: '*/*' } })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const blob = await res.blob()
    const objurl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = objurl; a.download = name || 'document'
    document.body.appendChild(a); a.click(); a.remove()
    setTimeout(() => URL.revokeObjectURL(objurl), 1000)
    if (btn) { btn.textContent = label; btn.disabled = false }
  } catch {
    if (btn) { btn.textContent = 'Echec'; btn.disabled = false }
  }
}

// ---- projects / practicals detail ----
function groupBlocks(item) {
  const mine = item.groups.find(g => g.isMine)
  const myGroup = mine ? `<h3 class="day-head">Mon groupe</h3><div class="kv"><span>${escapeHtml(mine.name)}</span><span class="muted">${escapeHtml(mine.students.join(', ') || '—')}</span></div>` : ''
  const all = item.groups.length ? `<h3 class="day-head">Groupes (${item.groups.length})</h3>${item.groups.map(g =>
    `<div class="kv"><span>${escapeHtml(g.name)}${g.isMine ? ' · moi' : ''}</span><span class="muted">${g.students.length} etu.</span></div>`).join('')}` : ''
  return myGroup + all
}
function stepsBlock(steps) {
  if (!steps.length) return ''
  return `<h3 class="day-head">Etapes</h3>${steps.map(s =>
    `<div class="kv"><span>${escapeHtml(s.title || 'Etape')}</span><span class="muted">${[s.deadline ? formatDateTime(s.deadline) : null, s.status].filter(Boolean).join(' · ') || '—'}</span></div>`).join('')}`
}
function docsBlock(docs) {
  return docs && docs.length ? `<h3 class="day-head">Fichiers</h3>${docs.map(docRow).join('')}` : ''
}
function openProjectModal(p) {
  const info = kvRows([
    ['Cours', p.courseName], ['Groupe', p.groupName], ['Mode', p.groupMode],
    ['Statut', p.status], ['Echeance', p.deadline ? formatDateTime(p.deadline) : null],
    ['Fichiers', p.fileCount ? String(p.fileCount) : null], ['Max / groupe', p.maxStudents != null ? String(p.maxStudents) : null]
  ])
  openModal(p.name || 'Projet', info + stepsBlock(p.steps) + groupBlocks(p) + docsBlock(p.documents), wireDocDownloads)
}
function openPracticalModal(p) {
  const info = kvRows([
    ['Cours', p.courseName], ['Debut', p.startsAt ? formatDateTime(p.startsAt) : null], ['Fin', p.endsAt ? formatDateTime(p.endsAt) : null],
    ['Salle', p.room], ['Statut', p.status]
  ])
  openModal(p.name || 'TP', info + stepsBlock(p.steps) + groupBlocks(p) + docsBlock(p.documents), wireDocDownloads)
}

// ---- courses ----
function renderCourses() {
  const all = state.snapshot.courses
  const cf = state.courseFilter || (state.courseFilter = { year: null, period: null })
  const years = [...new Set(all.map(c => c.year).filter(Boolean))].sort().reverse()
  if (cf.year === null && years.length) cf.year = years[0]
  const scoped0 = all.filter(c => !cf.year || c.year === cf.year)
  const periods = [...new Set(scoped0.map(c => c.period).filter(Boolean))].sort((a, b) => periodRank(b) - periodRank(a))
  const items = scoped0.filter(c => !cf.period || c.period === cf.period)

  const sel = (name, options, current) => `<select data-cf="${name}">${options.map(([v, l]) =>
    `<option value="${v}"${String(current) === String(v) ? ' selected' : ''}>${escapeHtml(l)}</option>`).join('')}</select>`

  content.innerHTML = `
    <div class="filters">
      ${years.length ? sel('year', [['', 'Toutes annees'], ...years.map(y => [y, y])], cf.year || '') : ''}
      ${periods.length ? sel('period', [['', 'Toutes periodes'], ...periods.map(p => [p, p])], cf.period || '') : ''}
    </div>
    <section class="card"><div class="list">${items.length
      ? items.map(c => `<article class="list-item is-clickable" data-course="${escapeHtml(c.id)}">
          <strong>${escapeHtml(c.name || 'Cours')}</strong>
          <span class="muted">${escapeHtml([c.teacher, c.period, c.fileCount ? `${c.fileCount} fichier(s)` : null].filter(Boolean).join(' · '))}</span>
        </article>`).join('')
      : '<p class="muted">Aucun cours.</p>'}</div></section>`
  content.querySelectorAll('[data-cf]').forEach(s => s.addEventListener('change', () => {
    const k = s.dataset.cf; cf[k] = s.value || null; if (k === 'year') cf.period = null; renderCourses()
  }))
  content.querySelectorAll('[data-course]').forEach(b => b.addEventListener('click', () => {
    const c = all.find(x => x.id === b.dataset.course)
    if (c) openCourseModal(c)
  }))
}
function openCourseModal(c) {
  const info = kvRows([['Enseignant', c.teacher], ['Annee', c.year], ['Periode', c.period], ['Fichiers', c.fileCount ? String(c.fileCount) : null]])
  const syl = c.syllabus
    ? `<h3 class="day-head">Syllabus</h3><div class="article">${escapeHtml(c.syllabus).replace(/\n/g, '<br>')}</div>`
    : `<button class="ghost-button" data-load-syllabus>Charger le syllabus</button>`
  const files = c.fileCount > 0 ? `<button class="ghost-button" data-load-files style="margin-top:8px">Voir les fichiers (${c.fileCount})</button><div id="course-files"></div>` : ''
  openModal(c.name || 'Cours', info + `<div id="course-syllabus">${syl}</div>` + files, () => {
    const sb = modalEl.querySelector('[data-load-syllabus]')
    if (sb) sb.addEventListener('click', async () => {
      sb.textContent = '…'
      try {
        const txt = parseCourseSyllabus(await api(`me/${encodeURIComponent(c.id)}/syllabus`))
        modalEl.querySelector('#course-syllabus').innerHTML = txt
          ? `<h3 class="day-head">Syllabus</h3><div class="article">${escapeHtml(txt).replace(/\n/g, '<br>')}</div>`
          : '<p class="muted">Aucun syllabus.</p>'
      } catch { sb.textContent = 'Echec' }
    })
    const fb = modalEl.querySelector('[data-load-files]')
    if (fb) fb.addEventListener('click', async () => {
      fb.textContent = '…'
      try {
        const docs = parseDocuments(await api(`me/${encodeURIComponent(c.id)}/files`)).map(d => ({ ...d, downloadUrl: d.downloadUrl || `me/${c.id}/files/${d.id}` }))
        modalEl.querySelector('#course-files').innerHTML = docs.length ? docs.map(docRow).join('') : '<p class="muted">Aucun fichier.</p>'
        wireDocDownloads()
        fb.remove()
      } catch { fb.textContent = 'Echec' }
    })
  })
}

// ---- practicals ----
function renderPracticals() {
  const items = state.snapshot.practicals.slice().sort((a, b) => (b.startsAt || 0) - (a.startsAt || 0))
  content.innerHTML = `<section class="card"><h2>TP</h2><div class="list">${items.length
    ? items.map(p => `<article class="list-item is-clickable" data-practical="${escapeHtml(p.id)}">
        <strong>${escapeHtml(p.name || 'TP')}</strong>
        <span class="muted">${escapeHtml([p.courseName, p.startsAt ? formatDateTime(p.startsAt) : null, p.status].filter(Boolean).join(' · '))}</span>
      </article>`).join('')
    : '<p class="muted">Aucun TP.</p>'}</div></section>`
  content.querySelectorAll('[data-practical]').forEach(b => b.addEventListener('click', () => {
    const p = items.find(x => x.id === b.dataset.practical)
    if (p) openPracticalModal(p)
  }))
}

// ---- documents ----
function renderDocuments() {
  const items = state.snapshot.documents.slice().sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
  content.innerHTML = `<section class="card"><h2>Documents</h2><div class="list">${items.length
    ? items.map(d => `<div class="kv"><span>${escapeHtml(d.title || d.fileName || 'Document')}<br><span class="muted" style="font-size:12px">${escapeHtml([d.category, d.year, d.updatedAt ? formatDate(d.updatedAt) : null].filter(Boolean).join(' · '))}</span></span>
        <button class="chip" data-doc="${escapeHtml(d.downloadUrl || '')}" data-name="${escapeHtml(d.fileName || d.title || 'document')}">Telecharger</button></div>`).join('')
    : '<p class="muted">Aucun document.</p>'}</div></section>`
  content.querySelectorAll('[data-doc]').forEach(b => b.addEventListener('click', () => downloadDocument(b.dataset.doc, b.dataset.name, b)))
}

// ---- directory ----
function renderDirectory() {
  const all = state.snapshot.directory
  const df = state.dirFilter || (state.dirFilter = { year: null, role: null })
  const years = [...new Set(all.map(p => p.year).filter(Boolean))].sort().reverse()
  if (df.year === null && years.length) df.year = years[0]
  const items = all.filter(p => (!df.year || p.year === df.year) && (!df.role || p.role === df.role))
    .sort((a, b) => a.displayName.localeCompare(b.displayName))

  const sel = (name, options, current) => `<select data-df="${name}">${options.map(([v, l]) =>
    `<option value="${v}"${String(current) === String(v) ? ' selected' : ''}>${escapeHtml(l)}</option>`).join('')}</select>`

  content.innerHTML = `
    <div class="filters">
      ${sel('role', [['', 'Tous'], ['Student', 'Etudiants'], ['Teacher', 'Enseignants']], df.role || '')}
      ${years.length ? sel('year', [['', 'Toutes annees'], ...years.map(y => [y, y])], df.year || '') : ''}
    </div>
    <section class="card"><div class="list">${items.length
      ? items.map(p => `<div class="kv"><span>${escapeHtml(p.displayName)}<br><span class="muted" style="font-size:12px">${escapeHtml([p.role === 'Teacher' ? 'Enseignant' : 'Etudiant', p.groupName].filter(Boolean).join(' · '))}</span></span>
          ${p.email ? `<a class="chip" href="mailto:${escapeHtml(p.email)}">Contact</a>` : ''}</div>`).join('')
      : '<p class="muted">Aucune personne.</p>'}</div></section>`
  content.querySelectorAll('[data-df]').forEach(s => s.addEventListener('change', () => { df[s.dataset.df] = s.value || null; renderDirectory() }))
}

// ---- events ----
function renderEvents() {
  const items = state.snapshot.events.slice().sort((a, b) => (b.date || 0) - (a.date || 0))
  content.innerHTML = `<section class="card"><h2>Evenements</h2><div class="list">${items.length
    ? items.map(e => `<article class="list-item is-clickable" data-eventid="${escapeHtml(e.id)}">
        <strong>${escapeHtml(e.title || 'Evenement')}</strong>
        <span class="muted">${escapeHtml([e.date ? formatDateTime(e.date) : null, e.location, e.subscribed ? 'Inscrit' : null].filter(Boolean).join(' · '))}</span>
      </article>`).join('')
    : '<p class="muted">Aucun evenement.</p>'}</div></section>`
  content.querySelectorAll('[data-eventid]').forEach(b => b.addEventListener('click', () => {
    const e = items.find(x => x.id === b.dataset.eventid)
    if (!e) return
    openModal(e.title || 'Evenement', kvRows([
      ['Date', e.date ? formatDateTime(e.date) : null], ['Lieu', e.location], ['Organisateur', e.organizer], ['Type', e.type],
      ['Inscriptions', e.subscriptionStart || e.subscriptionEnd ? `${e.subscriptionStart ? formatDate(e.subscriptionStart) : ''} → ${e.subscriptionEnd ? formatDate(e.subscriptionEnd) : ''}` : null],
      ['Statut', e.subscribed ? 'Inscrit' : 'Non inscrit']
    ]) + (e.description ? `<h3 class="day-head">Description</h3><p>${escapeHtml(e.description)}</p>` : ''))
  }))
}

function renderGrades() {
  const all = state.snapshot.grades
  const period = currentPeriod(all)
  const periods = [...new Set(all.map(g => g.period).filter(Boolean))].sort((a, b) => periodRank(b) - periodRank(a))
  const shown = state.gradesPeriod || period || periods[0] || null
  let scoped = shown ? all.filter(g => g.period === shown) : all

  if (state.sim) {
    scoped = withRecomputedMainGrades(withSimulatedValues(scoped, state.simValues), new Set(Object.keys(state.simValues)))
  }
  const summary = toGradeSummary(mainGrades(scoped))
  const items = mainGrades(scoped).slice().sort((a, b) => (b.date || 0) - (a.date || 0))

  const tabs = periods.length > 1 ? `<div class="chip-row">${periods.map(p =>
    `<button class="chip${p === shown ? ' is-active' : ''}" data-period="${escapeHtml(p)}">${escapeHtml(p)}</button>`).join('')}</div>` : ''

  const simBar = `<div class="chip-row">
    <button class="chip${state.sim ? ' is-active' : ''}" data-sim="toggle">${state.sim ? '● Simulation' : '○ Simulation'}</button>
    ${state.sim && Object.keys(state.simValues).length ? '<button class="chip" data-sim="reset">Reinitialiser</button>' : ''}
  </div>`

  content.innerHTML = `
    ${tabs}
    ${simBar}
    <section class="grid cols-2">
      ${metricCard(state.sim ? 'Moyenne simulee' : 'Moyenne', summary.weightedAverage != null ? summary.weightedAverage.toFixed(2).replace('.', ',') : '-', `${summary.gradedCount} notes${summary.incomplete ? ' · incomplet' : ''}`)}
      ${metricCard('GPA', summary.gpa != null ? summary.gpa.toFixed(2).replace('.', ',') : '-', 'sur 4')}
    </section>
    ${blockAveragesCard(mainGrades(scoped))}
    <section class="card"><h2>Notes</h2><div class="list">${items.length
      ? items.map(g => `<article class="list-item is-clickable" data-grade="${escapeHtml(g.id)}">
          <strong>${escapeHtml([g.courseName, g.subject].filter(Boolean).join(' — ') || 'Note')}</strong>
          <span class="${gradeColorClass(g.value, g.scale)}">${g.value != null
            ? `${formatNumber(g.value)}/${formatNumber(g.scale ?? 20)}${g.coefficient != null && g.coefficient > 0 ? ` · coef ${formatNumber(g.coefficient)}` : (isNotCounted(g) ? ' · N.C.' : '')}`
            : 'En attente'}</span>
        </article>`).join('')
      : '<p class="muted">Aucune donnee synchronisee.</p>'}</div></section>
  `
  content.querySelectorAll('[data-period]').forEach(btn => btn.addEventListener('click', () => { state.gradesPeriod = btn.dataset.period; renderGrades() }))
  content.querySelectorAll('[data-sim]').forEach(btn => btn.addEventListener('click', () => {
    if (btn.dataset.sim === 'toggle') state.sim = !state.sim
    else { state.simValues = {}; persistSim() }
    renderGrades()
  }))
  content.querySelectorAll('[data-grade]').forEach(btn => btn.addEventListener('click', () => {
    const g = scoped.find(x => x.id === btn.dataset.grade)
    if (g) openGradeModal(g, scoped)
  }))
}

function isNotCounted(g) { return g.coefficient === -1 }

function openGradeModal(grade, allGrades) {
  const isComponent = grade.id.includes('-cc-') || grade.id.includes('-exam')
  const components = gradeComponents(allGrades, grade)
  const cc = isComponent ? [] : components.filter(c => c.id.includes('-cc-'))
  const exam = isComponent ? null : components.find(c => c.id.includes('-exam'))
  const editable = (cc.length === 0 && !exam) ? [grade] : [...cc, ...(exam ? [exam] : [])]

  const blockKey = gradeBlockKey(grade)
  const blockField = !isComponent
    ? `<label class="field"><span>Bloc (regroupe les matieres pour une moyenne de bloc)</span>
        <input type="text" inputmode="numeric" data-block-key="${escapeHtml(blockKey)}" value="${escapeHtml(state.blocks[blockKey] || '')}" placeholder="ex: 1"></label>`
    : ''
  const head = `<p class="muted">${escapeHtml(grade.period || '')}</p>${blockField}`
  let body

  if (state.sim) {
    body = editable.map(g => `<label class="field">
        <span>${escapeHtml(g.subject || g.courseName || 'Note')} (coef ${formatNumber(g.coefficient)})</span>
        <input type="number" inputmode="decimal" step="0.01" min="0" max="${formatNumber(g.scale ?? 20)}"
          data-sim-id="${escapeHtml(g.id)}" value="${g.value != null ? g.value : ''}">
      </label>`).join('')
    body += `<div class="kv"><span class="muted">Moyenne matiere simulee</span><strong id="sim-course-avg">${formatCourseAvg(grade, components)}</strong></div>`
  } else {
    const ccRows = cc.map(c => `<div class="kv"><span>${escapeHtml(c.subject || 'CC')}</span><span class="${gradeColorClass(c.value, c.scale)}">${c.value != null ? `${formatNumber(c.value)}/${formatNumber(c.scale ?? 20)}` : '—'}</span></div>`).join('')
    const ccVals = cc.map(c => c.value).filter(v => v != null)
    const ccAvg = ccVals.length ? ccVals.reduce((a, b) => a + b, 0) / ccVals.length : null
    body = [
      ccRows ? `<h3 class="day-head">Contrôle continu</h3>${ccRows}${ccAvg != null ? `<div class="kv"><span class="muted">Moyenne CC</span><span>${formatNumber(ccAvg)}/20</span></div>` : ''}` : '',
      exam ? `<h3 class="day-head">Examen</h3><div class="kv"><span>${escapeHtml(exam.subject || 'Examen')}</span><span class="${gradeColorClass(exam.value, exam.scale)}">${exam.value != null ? `${formatNumber(exam.value)}/${formatNumber(exam.scale ?? 20)}` : '—'}</span></div>` : '',
      `<div class="kv"><span class="muted">Moyenne generale</span><strong class="${gradeColorClass(grade.value, grade.scale)}">${grade.value != null ? `${formatNumber(grade.value)}/${formatNumber(grade.scale ?? 20)}` : '—'}</strong></div>`,
      `<div class="kv"><span class="muted">Coefficient</span><span>${isNotCounted(grade) ? 'N.C.' : formatNumber(grade.coefficient)}</span></div>`,
      grade.date ? `<div class="kv"><span class="muted">Date</span><span>${escapeHtml(formatDate(grade.date))}</span></div>` : ''
    ].join('')
  }

  let blocksTouched = false
  openModal(grade.courseName || grade.subject || 'Note', head + body, () => {
    modalEl.querySelectorAll('[data-sim-id]').forEach(input => input.addEventListener('input', () => {
      const id = input.dataset.simId
      const v = input.value.trim().replace(',', '.')
      const n = v === '' ? null : Number(v)
      if (n == null || !Number.isFinite(n) || n < 0) delete state.simValues[id]
      else state.simValues[id] = n
      persistSim()
      const live = modalEl.querySelector('#sim-course-avg')
      if (live) live.textContent = formatCourseAvg(grade, components)
    }))
    const blockInput = modalEl.querySelector('[data-block-key]')
    if (blockInput) blockInput.addEventListener('input', () => {
      const key = blockInput.dataset.blockKey
      const v = blockInput.value.trim()
      if (v) state.blocks[key] = v; else delete state.blocks[key]
      localStorage.setItem('studly.pwa.blocks', JSON.stringify(state.blocks))
      blocksTouched = true
    })
  }, () => { if (state.sim || blocksTouched) renderGrades() })
}

function formatCourseAvg(grade, components) {
  const applied = withSimulatedValues(components, state.simValues)
  if (grade.id.includes('-cc-') || grade.id.includes('-exam')) {
    return formatNumber(state.simValues[grade.id] ?? grade.value)
  }
  const recomputed = withRecomputedMainGrades(applied, new Set(Object.keys(state.simValues))).find(g => g.id === grade.id)
  return recomputed && recomputed.value != null ? `${formatNumber(recomputed.value)}/20` : '—'
}

function persistSim() {
  localStorage.setItem('studly.pwa.sim', JSON.stringify(state.simValues))
}

function gradeColorClass(value, scale) {
  if (value == null) return 'muted'
  const norm = value / ((scale && scale > 0) ? scale : 20) * 20
  return norm >= 10 ? 'grade-good' : 'grade-bad'
}

function blockAveragesCard(grades) {
  const byBlock = new Map()
  for (const g of grades) {
    const block = state.blocks[gradeBlockKey(g)]
    if (!block) continue
    if (!byBlock.has(block)) byBlock.set(block, [])
    byBlock.get(block).push(g)
  }
  if (!byBlock.size) return ''
  const rows = [...byBlock.keys()]
    .sort((a, b) => ((parseInt(a, 10) || 1e9) - (parseInt(b, 10) || 1e9)) || a.localeCompare(b))
    .map(block => {
      const avg = toGradeSummary(byBlock.get(block)).weightedAverage
      return `<div class="kv"><span>Bloc ${escapeHtml(block)}</span><span class="${gradeColorClass(avg, 20)}">${avg != null ? formatNumber(avg) + '/20' : '—'}</span></div>`
    }).join('')
  return `<section class="card"><h2>Moyennes par bloc</h2>${rows}</section>`
}

function absYear(a) { const m = /\d{4}\s*-\s*\d{4}/.exec(a.period || ''); return m ? m[0].replace(/\s/g, '') : null }
function absSem(a) { const m = /semestre\s*\d/i.exec(a.period || ''); return m ? m[0].replace(/\s+/g, ' ').replace(/^./, c => c.toUpperCase()) : null }

function renderAbsences() {
  const all = state.snapshot.absences
  const f = state.absFilter || (state.absFilter = { year: null, sem: null, justified: null })
  const years = [...new Set(all.map(absYear).filter(Boolean))].sort().reverse()
  if (f.year === null && years.length) f.year = years[0]
  const sems = [...new Set(all.filter(a => !f.year || absYear(a) === f.year).map(absSem).filter(Boolean))].sort().reverse()

  const items = all.filter(a =>
    (!f.year || absYear(a) === f.year) &&
    (!f.sem || absSem(a) === f.sem) &&
    (f.justified === null || a.justified === f.justified)
  ).sort((a, b) => b.startsAt - a.startsAt)
  const unjustified = items.filter(a => !a.justified).length

  const sel = (name, options, current) => `<select data-abs="${name}">${options.map(([v, l]) =>
    `<option value="${v}"${String(current) === String(v) ? ' selected' : ''}>${escapeHtml(l)}</option>`).join('')}</select>`

  content.innerHTML = `
    <div class="filters">
      ${years.length ? sel('year', [['', 'Toutes annees'], ...years.map(y => [y, y])], f.year || '') : ''}
      ${sems.length ? sel('sem', [['', 'Tous semestres'], ...sems.map(s => [s, s])], f.sem || '') : ''}
      ${sel('justified', [['', 'Toutes'], ['true', 'Justifiees'], ['false', 'Non justifiees']], f.justified === null ? '' : String(f.justified))}
    </div>
    ${metricCard(`${items.length} absence${items.length > 1 ? 's' : ''}`, String(unjustified), 'non justifiees')}
    <section class="card"><div class="list">${items.length
      ? items.map(a => `<article class="list-item is-clickable" data-abs-id="${escapeHtml(a.id)}">
          <strong>${escapeHtml(a.courseName || 'Absence')}</strong>
          <span class="${a.justified ? 'grade-good' : 'grade-bad'}">${formatDateTime(a.startsAt)} · ${a.justified ? 'Justifiee' : 'Non justifiee'}</span>
        </article>`).join('')
      : '<p class="muted">Aucune absence.</p>'}</div></section>
  `
  content.querySelectorAll('[data-abs]').forEach(s => s.addEventListener('change', () => {
    const key = s.dataset.abs
    let v = s.value
    if (key === 'justified') f.justified = v === '' ? null : v === 'true'
    else { f[key] = v || null; if (key === 'year') f.sem = null }
    renderAbsences()
  }))
  content.querySelectorAll('[data-abs-id]').forEach(b => b.addEventListener('click', () => {
    const a = all.find(x => x.id === b.dataset.absId)
    if (a) openModal(a.courseName || 'Absence', kvRows([
      ['Debut', formatDateTime(a.startsAt)], ['Fin', formatDateTime(a.endsAt)],
      ['Statut', a.justified ? 'Justifiee' : 'Non justifiee'], ['Motif', a.reason], ['Periode', a.period]
    ]))
  }))
}

function renderProjects() {
  const now = Date.now()
  const items = state.snapshot.projects.slice()
    .sort((a, b) => (nextDeadline(a, now) ?? Infinity) - (nextDeadline(b, now) ?? Infinity))
  content.innerHTML = `<section class="card"><h2>Projets</h2><div class="list">${items.length
    ? items.map(p => `<article class="list-item is-clickable" data-project="${escapeHtml(p.id)}">
        <strong>${escapeHtml(p.name || 'Projet')}</strong>
        <span class="muted">${escapeHtml([p.courseName, p.deadline ? `Echeance ${formatDateTime(p.deadline)}` : null, p.status].filter(Boolean).join(' · '))}</span>
      </article>`).join('')
    : '<p class="muted">Aucun projet.</p>'}</div></section>`
  content.querySelectorAll('[data-project]').forEach(b => b.addEventListener('click', () => {
    const p = items.find(x => x.id === b.dataset.project)
    if (p) openProjectModal(p)
  }))
}

function renderNews() {
  const news = state.snapshot.news
  content.innerHTML = `<section class="card"><h2>Actualites</h2><div class="list">${news.length
    ? news.map(n => `<article class="list-item is-clickable" data-news="${escapeHtml(n.id)}">
        <strong>${escapeHtml(n.title || 'Actualite')}</strong>
        <span class="muted">${escapeHtml([n.publishedAt ? formatDateTime(n.publishedAt) : null, n.body].filter(Boolean).join(' · ').slice(0, 140))}</span>
      </article>`).join('')
    : '<p class="muted">Aucune actualite.</p>'}</div></section>`
  content.querySelectorAll('[data-news]').forEach(b => b.addEventListener('click', () => {
    const n = news.find(x => x.id === b.dataset.news)
    if (!n) return
    const bodyHtml = n.html
      ? `<div class="article">${sanitizeHtml(n.html)}</div>`
      : `<p>${escapeHtml(n.body || '')}</p>`
    openModal(n.title || 'Actualite', `${n.publishedAt ? `<p class="muted">${formatDateTime(n.publishedAt)}</p>` : ''}${bodyHtml}`)
  }))
}

// ---- domain-aware view helpers ----
function periodRank(period) {
  const year = Number((String(period).match(/\d{4}/) || [0])[0])
  const nums = String(period).match(/\d+/g) || []
  const last = Number(nums[nums.length - 1] || 0)
  return year * 100 + last
}

function currentPeriod(grades) {
  return grades.map(g => g.period).filter(Boolean).sort((a, b) => periodRank(b) - periodRank(a))[0] || null
}

function latestPeriodAbsences(absences) {
  const p = absences.map(a => a.period).filter(Boolean).sort((a, b) => periodRank(b) - periodRank(a))[0]
  return p ? absences.filter(a => a.period === p) : absences
}

function nextDeadline(project, now) {
  const candidates = [project.deadline, ...project.steps.map(s => s.deadline)]
    .filter(v => v != null && v > now)
  return candidates.length ? Math.min(...candidates) : null
}

function formatNumber(n) {
  if (n == null) return '-'
  return (Math.round(n * 100) / 100).toString().replace('.', ',')
}

function listCard(title, items, project) {
  const body = items.length
    ? items.map(item => {
        const v = project(item)
        return `<article class="list-item"><strong>${escapeHtml(v.title)}</strong><span class="muted">${escapeHtml(v.meta || '')}</span></article>`
      }).join('')
    : '<p class="muted">Aucune donnee synchronisee.</p>'
  return `<section class="card"><h2>${escapeHtml(title)}</h2><div class="list">${body}</div></section>`
}

function gradesSection(title, grades) {
  return listCard(title, grades, g => ({
    title: [g.courseName, g.subject].filter(Boolean).join(' — ') || 'Note',
    meta: g.value != null ? `${formatNumber(g.value)}/${formatNumber(g.scale ?? 20)}` : 'En attente'
  }))
}

function newsSection(news) {
  return listCard('Actualites', news, n => ({
    title: n.title || 'Actualite',
    meta: [n.publishedAt ? formatDateTime(n.publishedAt) : null, n.body].filter(Boolean).join(' · ').slice(0, 140)
  }))
}

async function refresh() {
  if (!state.session?.accessToken || state.loading) return
  state.loading = true
  syncButton.setAttribute('aria-busy', 'true')
  try {
    const profile = parseProfile(await api('me/profile'))
    const years = await resolveYears()
    const now = Date.now()
    const agendaRaw = await api(`me/agenda?start=${now - 86400000}&end=${now + 365 * 86400000}`).catch(() => null)
    const enc = encodeURIComponent
    const uid = profile.id
    const [grades, absences, projects, news, courses, documents, practicals, directory, events] = await Promise.all([
      collectYears(years, year => api(`me/${enc(year)}/grades`).then(json => parseGrades(json, year))),
      collectYears(years, year => api(`me/${enc(year)}/absences`).then(json => parseAbsences(json, year))),
      collectYears(years, year => api(`me/${enc(year)}/projects`).then(json => parseProjects(json, year, uid))),
      api('me/news').then(parseNews).catch(() => []),
      collectYears(years, year => api(`me/${enc(year)}/courses`).then(parseCourses)),
      collectYears(years, year => api(`me/${enc(year)}/annualDocuments`).then(json => parseDocuments(json, year))),
      collectYears(years, year => api(`me/${enc(year)}/practicals`).then(json => parsePracticals(json, year, uid))),
      collectYears(years, year => Promise.all([
        api(`me/${enc(year)}/students`).then(json => parseDirectory(json, 'Student', year)).catch(() => []),
        api(`me/${enc(year)}/teachers`).then(json => parseDirectory(json, 'Teacher', year)).catch(() => [])
      ]).then(([s, t]) => [...s, ...t])),
      api('me/events').then(parseEvents).catch(() => [])
    ])
    state.snapshot = {
      profile,
      agenda: parseAgenda(agendaRaw),
      grades,
      absences,
      projects,
      news,
      courses,
      documents,
      practicals,
      directory,
      events,
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
    const years = parseYears(await api('me/years'))
    return years.length ? years : [schoolYear()]
  } catch {
    return [schoolYear()]
  }
}

async function collectYears(years, loader) {
  const settled = await Promise.allSettled(years.map(year => loader(year)))
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
  const parsed = parseOAuthParams(location.hash)
  if (!parsed.accessToken) return
  saveSession(parsed)
  history.replaceState(null, '', location.pathname + location.search)
  refresh()
}

function parseTokenInput(input) {
  const value = input.trim()
  const parsed = parseOAuthParams(value)
  if (parsed.accessToken) return parsed
  return {
    accessToken: value.replace(/^Bearer\s+/i, ''),
    expiresAt: null
  }
}

function parseOAuthParams(value) {
  const rawParams = value.includes('#') ? value.substring(value.indexOf('#') + 1) : value
  const params = new URLSearchParams(rawParams.replace(/^\?/, ''))
  const accessToken = params.get('access_token')
  const expiresIn = Number(params.get('expires_in') || 0)
  return {
    accessToken: accessToken || '',
    expiresAt: accessToken && expiresIn ? new Date(Date.now() + expiresIn * 1000).toISOString() : null
  }
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
  return isBlockedRedirectUri(value) ? '' : value
}

function isBlockedRedirectUri(value) {
  try {
    const url = new URL(value)
    return ['localhost', '127.0.0.1', '::1', 'authentication.kordis.fr'].includes(url.hostname)
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

function resetLocalSessionIfRequested() {
  const params = new URLSearchParams(location.search)
  if (!params.has('reset')) return
  localStorage.removeItem(CONFIG.storageKey)
  localStorage.removeItem(CONFIG.cacheKey)
  localStorage.removeItem('studly.pwa.redirectUri')
  history.replaceState(null, '', location.pathname)
}

function emptySnapshot() {
  return { profile: {}, agenda: [], grades: [], absences: [], projects: [], news: [], courses: [], documents: [], practicals: [], directory: [], events: [], updatedAt: null }
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

const DAY = 86400000
function startOfDay(ms) { const d = new Date(ms); d.setHours(0, 0, 0, 0); return d.getTime() }
function mondayOf(ms) { const d = new Date(startOfDay(ms)); const wd = (d.getDay() + 6) % 7; return d.getTime() - wd * DAY }
function sameDay(a, b) { return startOfDay(toTime(a)) === startOfDay(toTime(b)) }
function formatDate(ms) { return new Intl.DateTimeFormat('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' }).format(new Date(toTime(ms))) }
function formatTime(ms) { return new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' }).format(new Date(toTime(ms))) }

// ---- modal ----
let modalEl = null
let modalOnClose = null
function openModal(title, bodyHtml, onMount, onClose) {
  if (!modalEl) {
    modalEl = document.createElement('div')
    modalEl.className = 'modal-overlay'
    modalEl.hidden = true
    modalEl.addEventListener('click', e => { if (e.target === modalEl) closeModal() })
    document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal() })
    document.body.appendChild(modalEl)
  }
  modalOnClose = onClose || null
  modalEl.innerHTML = `<div class="modal-card"><div class="modal-head"><h2>${escapeHtml(title)}</h2><button class="icon-btn" data-close aria-label="Fermer">✕</button></div><div class="modal-body">${bodyHtml}</div></div>`
  modalEl.hidden = false
  modalEl.querySelector('[data-close]').addEventListener('click', closeModal)
  if (onMount) onMount()
}
function closeModal() {
  if (!modalEl || modalEl.hidden) return
  modalEl.hidden = true
  modalEl.innerHTML = ''
  const cb = modalOnClose
  modalOnClose = null
  if (cb) cb()
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
