const data = {
  profile: {
    name: 'Emma Laurent',
    school: 'ESGI Paris',
    program: 'Mastère 1 - Architecture logicielle',
    sync: 'Aujourd’hui, 08:42'
  },
  agenda: [
    ['09:00', '10:30', 'Architecture cloud', 'Salle B302', 'Présentiel'],
    ['11:00', '12:30', 'Sécurité applicative', 'Amphi 4', 'Partiel blanc'],
    ['14:00', '17:00', 'Projet annuel', 'Lab 2', 'Groupe Atlas']
  ],
  grades: [
    ['API REST', 'Architecture logicielle', '16,5', 'Coef. 2'],
    ['Audit sécurité', 'Sécurité applicative', '14,0', 'Coef. 1'],
    ['Soutenance sprint', 'Projet annuel', '18,0', 'Coef. 3']
  ],
  projects: [
    ['Prototype mobile', 'Livrable UI + parcours utilisateur', 72, 'Vendredi'],
    ['Dossier architecture', 'Diagrammes, risques et choix techniques', 48, '12 juil.'],
    ['Veille techno', 'Synthèse IA embarquée', 30, '18 juil.']
  ],
  absences: [
    ['Sécurité applicative', 'Retard validé', '12 juin', 'Justifié'],
    ['Architecture cloud', 'Absence cours magistral', '4 juin', 'À régulariser'],
    ['Projet annuel', 'Retard atelier', '28 mai', 'Justifié']
  ],
  documents: [
    ['Certificat de scolarité', 'PDF - Année 2025-2026', 'Nouveau'],
    ['Relevé de notes', 'Semestre 1', 'Disponible'],
    ['Convention de stage', 'A compléter', 'Action']
  ],
  events: [
    ['Forum alternance', 'Campus Paris', '10 juil.'],
    ['Workshop IA produit', 'En ligne', '16 juil.'],
    ['Conférence cybersécurité', 'Amphi 2', '22 juil.']
  ],
  news: [
    ['Ouverture des inscriptions électives', 'Choix des modules jusqu’au 15 juillet.'],
    ['Maintenance MyGES', 'Accès perturbé dimanche entre 8h et 10h.'],
    ['Forum entreprises', 'Prépare ton profil et tes disponibilités.']
  ]
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
const demoButton = document.querySelector('#demo-button')
const view = document.querySelector('#view')
const pageTitle = document.querySelector('#page-title')
const breadcrumb = document.querySelector('#breadcrumb')
const navItems = [...document.querySelectorAll('[data-route]')]

loginForm.addEventListener('submit', event => {
  event.preventDefault()
  openPortal()
})
demoButton.addEventListener('click', openPortal)
window.addEventListener('hashchange', render)
render()

function openPortal() {
  if (!location.hash || location.hash === '#login') location.hash = '#dashboard'
  document.body.classList.add('is-authenticated')
  render()
}

function render() {
  const route = location.hash.replace('#', '') || 'dashboard'
  if (!location.hash || route === 'login') {
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
  return `
    <section class="grid dashboard-grid">
      <div class="stack">
        <article class="panel welcome">
          <div>
            <p class="eyebrow">Bonjour ${data.profile.name.split(' ')[0]}</p>
            <h2>${data.profile.program}</h2>
            <p>${data.profile.school} · ta journée, tes notes et tes échéances réunies dans une interface plus lisible.</p>
            <div class="pill-row">
              <span class="pill">Prochain cours 09:00</span>
              <span class="pill">Moyenne 15,8</span>
              <span class="pill">0 absence injustifiée</span>
            </div>
          </div>
          <div class="campus-card" aria-hidden="true"></div>
        </article>
        <section class="grid cols-3">
          ${metric('Moyenne générale', '15,8', '29 notes suivies')}
          ${metric('Assiduité', '98%', '5 absences au total')}
          ${metric('Événements', '26', 'Vie étudiante')}
        </section>
        <section class="grid portal-grid">
          ${portalTile('Plannings', 'Semaine courante', '6 juillet - 12 juillet')}
          ${portalTile('Notes et absences', 'Semestre 3', '29 notes, 5 absences')}
          ${portalTile('Documents', 'Scolarite', 'Certificats et supports')}
          ${portalTile('Relations entreprises', 'Stages', 'Conventions et suivi')}
        </section>
        <section class="panel">
          <h2>Aujourd’hui</h2>
          <div class="list">${data.agenda.map(eventCard).join('')}</div>
        </section>
      </div>
      <aside class="stack">
        <section class="panel">
          <h2>Dernières notes</h2>
          <div class="list">${data.grades.map(gradeRow).join('')}</div>
        </section>
        <section class="panel">
          <h2>Actualités</h2>
          <div class="list">${data.news.map(newsRow).join('')}</div>
        </section>
      </aside>
    </section>
  `
}

function renderAgenda() {
  return `
    <section class="stack">
      <div class="pill-row">
        <button class="pill-button" type="button">Aujourd'hui</button>
        <button class="pill-button is-active" type="button">Semaine</button>
        <button class="pill-button" type="button">Mois</button>
        <button class="pill-button" type="button">Actualiser</button>
      </div>
      <section class="panel">
        <h2>6 juillet - 12 juillet 2026</h2>
        ${weekPlanner()}
      </section>
    </section>
  `
}

function renderGrades() {
  return `
    <section class="grid cols-2">
      ${metric('Moyenne semestre', '15,8', '8 notes publiées')}
      ${metric('Meilleure matière', 'Projet annuel', '18,0 / 20')}
    </section>
    <section class="panel">
      <h2>Notes</h2>
      ${gradesTable()}
    </section>
  `
}

function renderProjects() {
  return `
    <section class="grid cols-3">
      ${data.projects.map(([title, desc, progress, due]) => `
        <article class="metric">
          <small>${due}</small>
          <h3>${title}</h3>
          <p class="muted">${desc}</p>
          <div class="progress"><span style="width:${progress}%"></span></div>
        </article>
      `).join('')}
    </section>
  `
}

function renderAbsences() {
  return `
    <section class="grid cols-3">
      ${metric('Absences', '5', 'Année 2025')}
      ${metric('À régulariser', '1', 'Action requise')}
      ${metric('Retards', '2', 'Sans impact')}
    </section>
    <section class="panel">
      <h2>Historique</h2>
      <div class="list">
        ${data.absences.map(([course, detail, date, state]) => `
          <article class="table-row">
            <div>
              <h3>${course}</h3>
              <span class="muted">${detail} · ${date}</span>
            </div>
            <span class="tag">${state}</span>
          </article>
        `).join('')}
      </div>
    </section>
  `
}

function renderDocuments() {
  return `
    <section class="panel">
      <h2>Documents utiles</h2>
      <div class="list">
        ${data.documents.map(([title, desc, state]) => `
          <article class="table-row doc-row">
            <span class="doc-icon">PDF</span>
            <div>
              <h3>${title}</h3>
              <span class="muted">${desc}</span>
            </div>
            <span class="tag">${state}</span>
          </article>
        `).join('')}
      </div>
    </section>
  `
}

function renderEvents() {
  return `
    <section class="grid cols-2">
      ${metric('Événements ouverts', '26', 'Inscriptions disponibles')}
      ${metric('Cette semaine', '3', 'À ne pas manquer')}
    </section>
    <section class="panel">
      <h2>Vie étudiante</h2>
      <div class="list">
        ${data.events.map(([title, place, date]) => `
          <article class="table-row">
            <div>
              <h3>${title}</h3>
              <span class="muted">${place}</span>
            </div>
            <span class="tag">${date}</span>
          </article>
        `).join('')}
      </div>
    </section>
  `
}

function metric(label, value, detail) {
  return `
    <article class="metric">
      <small>${label}</small>
      <strong>${value}</strong>
      <em>${detail}</em>
    </article>
  `
}

function portalTile(title, detail, meta) {
  const route = title.startsWith('Planning') ? 'agenda' : title.startsWith('Notes') ? 'grades' : title.startsWith('Documents') ? 'documents' : 'projects'
  return `
    <a class="portal-tile" href="#${route}">
      <span>${title}</span>
      <strong>${detail}</strong>
      <small class="muted">${meta}</small>
    </a>
  `
}

function weekPlanner() {
  const days = ['Lun. 6/07', 'Mar. 7/07', 'Mer. 8/07', 'Jeu. 9/07', 'Ven. 10/07']
  const slots = ['09:00', '11:00', '14:00', '16:00']
  const events = {
    '0-0': ['Architecture cloud', 'B302'],
    '1-1': ['Design thinking', 'A118'],
    '2-0': ['Securite applicative', 'Amphi 4'],
    '2-2': ['Projet annuel', 'Lab 2'],
    '4-1': ['Scripting Python', 'C204']
  }
  return `
    <div class="week-board">
      <div class="week-head week-cell"></div>
      ${days.map(day => `<div class="week-head week-cell">${day}</div>`).join('')}
      ${slots.map((slot, row) => `
        <div class="week-time">${slot}</div>
        ${days.map((day, col) => {
          const event = events[`${col}-${row}`]
          return `<div class="week-cell">${event ? `<div class="week-event"><strong>${event[0]}</strong><span>${event[1]}</span></div>` : ''}</div>`
        }).join('')}
      `).join('')}
    </div>
  `
}

function gradesTable() {
  const rows = [
    ['T3 - open', 'Mme FILIOL', '1.00', '1.00', '20', '20.00'],
    ['T3 - Scripting python', 'M. RAYNAL', '2.00', '2.00', '20,0', '20.00'],
    ['T3 - projet annuel', 'M. SANANES', '3.00', '3.00', '-', '-'],
    ['T3 - securite du code', 'M. MILANO', '2.00', '2.00', '-', '-']
  ]
  return `
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
        ${rows.map(row => `<tr>${row.map(cell => `<td>${cell}</td>`).join('')}</tr>`).join('')}
      </tbody>
    </table>
  `
}

function eventCard([start, end, title, room, tag]) {
  return `
    <article class="event-card">
      <span class="time-chip">${start}<br>${end}</span>
      <div>
        <h3>${title}</h3>
        <span class="muted">${room}</span>
      </div>
      <span class="tag">${tag}</span>
    </article>
  `
}

function gradeRow([title, course, score, coef]) {
  const warn = Number(score.replace(',', '.')) < 10 ? ' warn' : ''
  return `
    <article class="table-row">
      <div>
        <h3>${title}</h3>
        <span class="muted">${course} · ${coef}</span>
      </div>
      <strong class="score${warn}">${score}</strong>
    </article>
  `
}

function newsRow([title, body]) {
  return `
    <article class="table-row">
      <div>
        <h3>${title}</h3>
        <span class="muted">${body}</span>
      </div>
    </article>
  `
}
