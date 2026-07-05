const API_BASE = 'https://api.kordis.fr/'
const SNAPSHOT_KEY = 'myges.real.snapshot'
const TOKEN_KEY = 'myges.real.token'

const { createApp, computed, reactive, ref, provide, proxyRefs } = Vue

createApp({
  setup() {
    const token = ref('')
    const error = ref('')
    const syncError = ref('')
    const loading = ref(false)
    const route = ref(routeFromHash())
    const state = reactive({
      gradePeriod: 'all',
      absenceStatus: 'all',
      projectQuery: '',
      documentType: 'all',
      eventFilter: 'all'
    })
    const data = reactive({
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
    })

    const nav = [
      { route: 'dashboard', label: 'Accueil', code: '01' },
      { route: 'agenda', label: 'Plannings', code: '02' },
      { route: 'grades', label: 'Notes', code: '03' },
      { route: 'absences', label: 'Absences', code: '!' },
      { route: 'projects', label: 'Projets', code: '04' },
      { route: 'documents', label: 'Docs', code: '05' },
      { route: 'events', label: 'Events', code: '06' }
    ]

    const authenticated = computed(() => Boolean(data.profile))
    const profile = computed(() => data.profile || { name: 'Profil MyGES', school: 'MyGES', program: 'Espace etudiant' })
    const title = computed(() => nav.find(item => item.route === route.value)?.label || 'Accueil')
    const initials = computed(() => initialsFrom(profile.value.name))
    const sortedAgenda = computed(() => data.agenda.slice().sort((a, b) => Number(eventStart(a) || 0) - Number(eventStart(b) || 0)))
    const gradePeriods = computed(() => ['all', ...unique(data.grades.map(gradePeriod))])
    const filteredGrades = computed(() => data.grades.filter(grade => state.gradePeriod === 'all' || gradePeriod(grade) === state.gradePeriod))
    const filteredAbsences = computed(() => data.absences.filter(absence => state.absenceStatus === 'all' || absenceStatus(absence) === state.absenceStatus))
    const filteredProjects = computed(() => data.projects.filter(project => projectTitle(project).toLowerCase().includes(state.projectQuery.toLowerCase())))
    const documentTypes = computed(() => ['all', ...unique(data.documents.map(documentType))])
    const filteredDocuments = computed(() => data.documents.filter(document => state.documentType === 'all' || documentType(document) === state.documentType))
    const eventTypes = computed(() => ['all', ...unique(data.events.map(eventType))])
    const filteredEvents = computed(() => data.events.filter(event => state.eventFilter === 'all' || eventType(event) === state.eventFilter))
    const hasContent = computed(() => hasAnyData(data))
    const syncLabel = computed(() => {
      if (loading.value) return 'Chargement API...'
      if (syncError.value) return 'Erreur API'
      if (hasContent.value) return 'Donnees API chargees'
      if (data.profile) return 'Profil charge, donnees vides'
      return 'Non connecte'
    })
    const syncDotClass = computed(() => {
      if (loading.value) return 'bg-amber-400'
      if (syncError.value) return 'bg-red-500'
      if (hasContent.value) return 'bg-emerald-500'
      return 'bg-slate-300'
    })
    const currentView = computed(() => ({
      dashboard: 'dashboard-view',
      agenda: 'agenda-view',
      grades: 'grades-view',
      absences: 'absences-view',
      projects: 'projects-view',
      documents: 'documents-view',
      events: 'events-view'
    })[route.value] || 'dashboard-view')

    window.addEventListener('hashchange', () => {
      route.value = routeFromHash()
    })
    resetSessionIfRequested()
    restoreSnapshot()

    async function login() {
      error.value = ''
      syncError.value = ''
      const cleanToken = token.value.trim().replace(/^bearer\s+/i, '')
      if (!cleanToken) {
        error.value = 'Colle ton bearer Kordis/MyGES pour charger de vraies donnees.'
        return
      }

      loading.value = true
      try {
        await loadRealData(cleanToken)
        sessionStorage.setItem(TOKEN_KEY, cleanToken)
        sessionStorage.setItem(SNAPSHOT_KEY, JSON.stringify(snapshot()))
        if (!location.hash || route.value === 'login') location.hash = '#dashboard'
        route.value = routeFromHash()
      } catch {
        error.value = "Connexion impossible avec ce bearer. Verifie le token ou l'acces CORS de l'API Kordis."
        syncError.value = error.value
      } finally {
        loading.value = false
      }
    }

    async function refresh() {
      error.value = ''
      syncError.value = ''
      const savedToken = sessionStorage.getItem(TOKEN_KEY)
      if (!savedToken) {
        data.profile = null
        route.value = 'login'
        location.hash = '#login'
        error.value = 'Reconnecte-toi pour relancer le fetch API.'
        return
      }

      loading.value = true
      try {
        await loadRealData(savedToken)
        sessionStorage.setItem(SNAPSHOT_KEY, JSON.stringify(snapshot()))
      } catch {
        syncError.value = "Rechargement impossible depuis l'API Kordis."
      } finally {
        loading.value = false
      }
    }

    async function loadRealData(cleanToken) {
      const headers = { Accept: 'application/json', Authorization: `Bearer ${cleanToken}` }
      const get = path => fetch(`${API_BASE}${path}`, { headers })
        .then(response => {
          if (!response.ok) throw new Error(`HTTP ${response.status}`)
          return response.json()
        })
        .then(unwrap)

      data.profile = profileFromApi(await get('me/profile'))
      data.years = toArray(await get('me/years')).map(yearValue).filter(Boolean)
      const years = data.years.length ? data.years : [String(new Date().getFullYear())]
      const start = Math.floor((Date.now() - 7 * 86400000) / 1000)
      const end = Math.floor((Date.now() + 28 * 86400000) / 1000)
      const [agenda, documents, news, events, yearData] = await Promise.all([
        get(`me/agenda?start=${start}&end=${end}`).catch(() => []),
        get('me/annualDocuments').catch(() => []),
        get('me/news').catch(() => []),
        get('me/events').catch(() => []),
        Promise.all(years.map(year => Promise.all([
          get(`me/${year}/grades`).catch(() => []),
          get(`me/${year}/absences`).catch(() => []),
          get(`me/${year}/projects`).catch(() => [])
        ])))
      ])

      data.agenda = toArray(agenda)
      data.grades = yearData.flatMap(([grades]) => toArray(grades))
      data.absences = yearData.flatMap(([, absences]) => toArray(absences))
      data.projects = yearData.flatMap(([, , projects]) => toArray(projects))
      data.documents = toArray(documents)
      data.news = toArray(news)
      data.events = toArray(events)
      data.updatedAt = Date.now()
    }

    function snapshot() {
      return {
        profile: data.profile,
        years: data.years,
        agenda: data.agenda,
        grades: data.grades,
        absences: data.absences,
        projects: data.projects,
        documents: data.documents,
        events: data.events,
        news: data.news,
        updatedAt: data.updatedAt
      }
    }

    function restoreSnapshot() {
      const saved = sessionStorage.getItem(SNAPSHOT_KEY)
      if (saved) {
        try {
          const parsed = JSON.parse(saved)
          if (parsed.profile && hasAnyData(parsed)) {
            Object.assign(data, parsed)
          } else {
            sessionStorage.removeItem(SNAPSHOT_KEY)
          }
        } catch {
          sessionStorage.removeItem(SNAPSHOT_KEY)
        }
      }
      if (sessionStorage.getItem(TOKEN_KEY) && (!data.profile || !hasAnyData(data))) {
        refresh()
      }
    }

    function resetSessionIfRequested() {
      if (!new URLSearchParams(location.search).has('reset')) return
      sessionStorage.removeItem(SNAPSHOT_KEY)
      sessionStorage.removeItem(TOKEN_KEY)
      history.replaceState(null, '', location.pathname)
    }

    const ctx = {
      token,
      error,
      syncError,
      loading,
      route,
      state,
      data,
      nav,
      authenticated,
      profile,
      title,
      initials,
      sortedAgenda,
      gradePeriods,
      filteredGrades,
      filteredAbsences,
      filteredProjects,
      documentTypes,
      filteredDocuments,
      eventTypes,
      filteredEvents,
      hasContent,
      syncLabel,
      syncDotClass,
      currentView,
      login,
      refresh,
      firstName,
      formatDateTime,
      formatTime,
      eventStart,
      eventEnd,
      eventTitle,
      eventRoom,
      eventType,
      gradeTitle,
      gradePeriod,
      gradeEcts,
      teacherName,
      absenceStatus,
      projectTitle,
      documentType,
      valueOf,
      sum
    }
    provide('ctx', proxyRefs(ctx))
    return ctx
  }
})
  .mixin(viewContextMixin())
  .component('metric-card', {
    props: ['label', 'value', 'detail'],
    template: `
      <article class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <small class="text-xs font-black uppercase tracking-wide text-slate-500">{{ label }}</small>
        <strong class="mt-3 block text-3xl font-black text-slate-900">{{ value }}</strong>
        <em class="mt-2 block text-sm font-extrabold not-italic text-myges-500">{{ detail }}</em>
      </article>
    `
  })
  .component('empty-state', {
    template: `<p class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-sm font-bold text-slate-500">Aucune donnee renvoyee par MyGES pour cette rubrique.</p>`
  })
  .component('dashboard-view', {
    template: `
      <section class="grid gap-4 xl:grid-cols-[1.25fr_.75fr]">
        <div class="grid gap-4">
          <article class="rounded-lg bg-myges-700 p-6 text-white shadow-lg shadow-myges-900/10">
            <p class="text-xs font-black uppercase tracking-wide text-white/70">Bonjour {{ firstName(profile.name) }}</p>
            <h2 class="mt-2 text-2xl font-black">{{ profile.program || 'Espace etudiant' }}</h2>
            <p v-if="!hasContent" class="mt-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm font-black text-amber-800">Profil charge, mais aucun endpoint de donnees n'a encore renvoye de contenu. Relance avec Recharger.</p>
            <p class="mt-2 text-sm font-medium leading-6 text-white/80">{{ profile.school || 'MyGES' }} · donnees chargees depuis ton compte Kordis.</p>
            <div class="mt-5 flex flex-wrap gap-2">
              <span class="rounded-full bg-white/15 px-3 py-2 text-xs font-black">{{ sortedAgenda[0] ? 'Prochain cours ' + formatDateTime(eventStart(sortedAgenda[0])) : 'Aucun cours renvoye' }}</span>
              <span class="rounded-full bg-white/15 px-3 py-2 text-xs font-black">{{ data.grades.length }} matieres</span>
              <span class="rounded-full bg-white/15 px-3 py-2 text-xs font-black">{{ data.absences.length }} absences</span>
            </div>
          </article>
          <section class="grid gap-3 sm:grid-cols-3">
            <metric-card label="Planning" :value="String(data.agenda.length)" detail="elements renvoyes"></metric-card>
            <metric-card label="Notes" :value="String(data.grades.length)" detail="matieres renvoyees"></metric-card>
            <metric-card label="Projets" :value="String(data.projects.length)" detail="projets renvoyes"></metric-card>
          </section>
          <section class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <a class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-myges-500" href="#agenda"><span class="text-sm font-black text-myges-500">Plannings</span><strong class="mt-2 block">Semaine courante</strong><small class="text-slate-500">{{ data.agenda.length }} elements</small></a>
            <a class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-myges-500" href="#grades"><span class="text-sm font-black text-myges-500">Notes et absences</span><strong class="mt-2 block">{{ data.years[0] || 'Annee' }}</strong><small class="text-slate-500">{{ data.grades.length }} notes, {{ data.absences.length }} absences</small></a>
            <a class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-myges-500" href="#documents"><span class="text-sm font-black text-myges-500">Documents</span><strong class="mt-2 block">Scolarite</strong><small class="text-slate-500">{{ data.documents.length }} documents</small></a>
            <a class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-myges-500" href="#projects"><span class="text-sm font-black text-myges-500">Projets pedagogiques</span><strong class="mt-2 block">Travaux</strong><small class="text-slate-500">{{ data.projects.length }} projets</small></a>
          </section>
          <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h2 class="mb-3 text-lg font-black">Aujourd'hui</h2>
            <div v-if="sortedAgenda.length" class="grid gap-2">
              <article v-for="event in sortedAgenda.slice(0, 5)" class="grid gap-3 rounded-lg border border-slate-200 p-3 sm:grid-cols-[92px_1fr_auto] sm:items-center">
                <span class="rounded-lg bg-myges-50 px-3 py-2 text-center text-sm font-black text-myges-700">{{ formatTime(eventStart(event)) }}<br>{{ formatTime(eventEnd(event)) }}</span>
                <div><h3 class="font-black">{{ eventTitle(event) }}</h3><p class="text-sm font-bold text-slate-500">{{ eventRoom(event) || '-' }}</p></div>
                <span class="justify-self-start rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ eventType(event) }}</span>
              </article>
            </div>
            <empty-state v-else></empty-state>
          </section>
        </div>
        <aside class="grid content-start gap-4">
          <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h2 class="mb-3 text-lg font-black">Dernieres notes</h2>
            <div v-if="data.grades.length" class="grid gap-2">
              <article v-for="grade in data.grades.slice(0, 4)" class="flex items-center justify-between gap-3 rounded-lg border border-slate-200 p-3">
                <div><h3 class="font-black">{{ gradeTitle(grade) }}</h3><p class="text-sm font-bold text-slate-500">{{ teacherName(grade) }}</p></div>
                <strong class="text-myges-500">{{ valueOf(grade, ['average', 'ccaverage']) || '-' }}</strong>
              </article>
            </div>
            <empty-state v-else></empty-state>
          </section>
          <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h2 class="mb-3 text-lg font-black">Actualites</h2>
            <div v-if="data.news.length" class="grid gap-2">
              <article v-for="item in data.news.slice(0, 5)" class="rounded-lg border border-slate-200 p-3">
                <h3 class="font-black">{{ valueOf(item, ['title', 'name', 'label']) || 'Actualite' }}</h3>
                <p class="text-sm font-bold text-slate-500">{{ valueOf(item, ['summary', 'description']) }}</p>
              </article>
            </div>
            <empty-state v-else></empty-state>
          </section>
        </aside>
      </section>
    `
  })
  .component('agenda-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <strong>{{ data.agenda.length }} elements de planning</strong>
          <span class="text-sm font-bold text-slate-500">Mise a jour {{ formatDateTime(data.updatedAt) }}</span>
        </div>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Planning</h2>
          <div v-if="sortedAgenda.length" class="grid gap-2">
            <article v-for="event in sortedAgenda" class="grid gap-3 rounded-lg border border-slate-200 p-3 sm:grid-cols-[92px_1fr_auto] sm:items-center">
              <span class="rounded-lg bg-myges-50 px-3 py-2 text-center text-sm font-black text-myges-700">{{ formatTime(eventStart(event)) }}<br>{{ formatTime(eventEnd(event)) }}</span>
              <div><h3 class="font-black">{{ eventTitle(event) }}</h3><p class="text-sm font-bold text-slate-500">{{ eventRoom(event) || '-' }}</p></div>
              <span class="justify-self-start rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ eventType(event) }}</span>
            </article>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .component('grades-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <label class="grid min-w-72 gap-1 text-xs font-black uppercase tracking-wide text-slate-500">Periode
            <select v-model="state.gradePeriod" class="h-11 rounded-lg border border-slate-200 px-3 text-sm text-slate-900"><option v-for="period in gradePeriods" :value="period">{{ period === 'all' ? 'Tous' : period }}</option></select>
          </label>
          <span class="text-sm font-bold text-slate-500">{{ filteredGrades.length }} matieres</span>
        </div>
        <section class="grid gap-3 sm:grid-cols-2">
          <metric-card label="Matieres" :value="String(filteredGrades.length)" detail="renvoyees par MyGES"></metric-card>
          <metric-card label="ECTS" :value="String(sum(filteredGrades.map(gradeEcts)))" detail="total affiche"></metric-card>
        </section>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Notes</h2>
          <div v-if="filteredGrades.length" class="overflow-x-auto">
            <table class="w-full min-w-[680px] border-collapse text-sm">
              <thead><tr class="bg-myges-500 text-left text-white"><th class="p-3">Matiere</th><th class="p-3">Intervenant</th><th class="p-3">Coef.</th><th class="p-3">ECTS</th><th class="p-3">Exam</th><th class="p-3">Moyenne</th></tr></thead>
              <tbody><tr v-for="grade in filteredGrades" class="border-b border-slate-200 even:bg-slate-50"><td class="p-3 font-bold">{{ gradeTitle(grade) }}</td><td class="p-3">{{ teacherName(grade) }}</td><td class="p-3">{{ valueOf(grade, ['coef']) || '-' }}</td><td class="p-3">{{ valueOf(grade, ['ects']) || '-' }}</td><td class="p-3">{{ valueOf(grade, ['exam', 'letter_mark']) || '-' }}</td><td class="p-3 font-black text-myges-500">{{ valueOf(grade, ['average', 'ccaverage']) || '-' }}</td></tr></tbody>
            </table>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .component('absences-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <label class="grid min-w-52 gap-1 text-xs font-black uppercase tracking-wide text-slate-500">Justifie
            <select v-model="state.absenceStatus" class="h-11 rounded-lg border border-slate-200 px-3 text-sm text-slate-900"><option value="all">Tous</option><option>Oui</option><option>Non</option></select>
          </label>
          <span class="text-sm font-bold text-slate-500">{{ filteredAbsences.length }} absences</span>
        </div>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Historique</h2>
          <div v-if="filteredAbsences.length" class="grid gap-2">
            <article v-for="absence in filteredAbsences" class="flex items-center justify-between gap-3 rounded-lg border border-slate-200 p-3"><div><h3 class="font-black">{{ valueOf(absence, ['course', 'course_name', 'matter']) || 'Absence' }}</h3><p class="text-sm font-bold text-slate-500">{{ formatDateTime(valueOf(absence, ['date', 'start_date', 'starts_at'])) || '-' }}</p></div><span class="rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ absenceStatus(absence) }}</span></article>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .component('projects-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <label class="grid min-w-72 gap-1 text-xs font-black uppercase tracking-wide text-slate-500">Recherche
            <input v-model="state.projectQuery" class="h-11 rounded-lg border border-slate-200 px-3 text-sm text-slate-900" placeholder="matiere, projet...">
          </label>
          <span class="text-sm font-bold text-slate-500">{{ filteredProjects.length }} projets</span>
        </div>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Projets pedagogiques</h2>
          <div v-if="filteredProjects.length" class="overflow-x-auto">
            <table class="w-full min-w-[680px] border-collapse text-sm">
              <thead><tr class="bg-myges-500 text-left text-white"><th class="p-3">Date</th><th class="p-3">Matiere</th><th class="p-3">Projet</th><th class="p-3">Actions</th></tr></thead>
              <tbody><tr v-for="project in filteredProjects" class="border-b border-slate-200 even:bg-slate-50"><td class="p-3">{{ formatDateTime(valueOf(project, ['update_date', 'project_create_date'])) }}</td><td class="p-3">{{ valueOf(project, ['course_name']) || '-' }}</td><td class="p-3 font-bold">{{ projectTitle(project) }}</td><td class="p-3"><span class="rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ project.groups?.length ? 'Groupe' : 'Voir' }}</span></td></tr></tbody>
            </table>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .component('documents-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <label class="grid min-w-52 gap-1 text-xs font-black uppercase tracking-wide text-slate-500">Type
            <select v-model="state.documentType" class="h-11 rounded-lg border border-slate-200 px-3 text-sm text-slate-900"><option v-for="type in documentTypes" :value="type">{{ type === 'all' ? 'Tous' : type }}</option></select>
          </label>
          <span class="text-sm font-bold text-slate-500">{{ filteredDocuments.length }} documents</span>
        </div>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Documents</h2>
          <div v-if="filteredDocuments.length" class="grid gap-2">
            <article v-for="document in filteredDocuments" class="grid gap-3 rounded-lg border border-slate-200 p-3 sm:grid-cols-[52px_1fr_auto] sm:items-center"><span class="grid h-11 w-11 place-items-center rounded-lg bg-myges-50 text-xs font-black text-myges-700">DOC</span><div><h3 class="font-black">{{ valueOf(document, ['name', 'title', 'label']) || 'Document' }}</h3><p class="text-sm font-bold text-slate-500">{{ documentType(document) }}</p></div><span class="justify-self-start rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ valueOf(document, ['status', 'year']) || 'MyGES' }}</span></article>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .component('events-view', {
    template: `
      <section class="grid gap-4">
        <div class="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <label class="grid min-w-52 gap-1 text-xs font-black uppercase tracking-wide text-slate-500">Categorie
            <select v-model="state.eventFilter" class="h-11 rounded-lg border border-slate-200 px-3 text-sm text-slate-900"><option v-for="type in eventTypes" :value="type">{{ type === 'all' ? 'Tous' : type }}</option></select>
          </label>
          <span class="text-sm font-bold text-slate-500">{{ filteredEvents.length }} evenements</span>
        </div>
        <section class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="mb-3 text-lg font-black">Vie etudiante</h2>
          <div v-if="filteredEvents.length" class="grid gap-2">
            <article v-for="event in filteredEvents" class="flex items-center justify-between gap-3 rounded-lg border border-slate-200 p-3"><div><h3 class="font-black">{{ valueOf(event, ['name', 'title', 'label']) || 'Evenement' }}</h3><p class="text-sm font-bold text-slate-500">{{ valueOf(event, ['place', 'location', 'campus']) || '-' }}</p></div><span class="rounded-full bg-myges-50 px-3 py-1 text-xs font-black text-myges-700">{{ formatDateTime(valueOf(event, ['date', 'start_date', 'starts_at'])) || eventType(event) }}</span></article>
          </div>
          <empty-state v-else></empty-state>
        </section>
      </section>
    `
  })
  .mount('#app')

function viewContextMixin() {
  const keys = [
    'state',
    'data',
    'profile',
    'sortedAgenda',
    'gradePeriods',
    'filteredGrades',
    'filteredAbsences',
    'filteredProjects',
    'documentTypes',
    'filteredDocuments',
    'eventTypes',
    'filteredEvents',
    'hasContent',
    'firstName',
    'formatDateTime',
    'formatTime',
    'eventStart',
    'eventEnd',
    'eventTitle',
    'eventRoom',
    'eventType',
    'gradeTitle',
    'gradeEcts',
    'teacherName',
    'absenceStatus',
    'projectTitle',
    'documentType',
    'valueOf',
    'sum'
  ]
  return {
    inject: {
      ctx: {
        default: null
      }
    },
    computed: Object.fromEntries(keys.map(key => [key, function () {
      return this.ctx?.[key]
    }]))
  }
}

function routeFromHash() {
  return location.hash.replace('#', '') || 'login'
}

function unwrap(payload) {
  return payload?.result ?? payload ?? []
}

function toArray(value) {
  if (Array.isArray(value)) return value
  if (value && typeof value === 'object') return Object.values(value).flat().filter(item => item && typeof item === 'object')
  return []
}

function hasAnyData(source) {
  return ['agenda', 'grades', 'absences', 'projects', 'documents', 'events', 'news']
    .some(key => Array.isArray(source?.[key]) && source[key].length > 0)
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

function initialsFrom(name) {
  const parts = name.split(/\s+/).filter(Boolean)
  return ((parts[0]?.[0] || 'M') + (parts[1]?.[0] || 'G')).toUpperCase()
}

function firstName(name) {
  return name.split(/\s+/).filter(Boolean)[0] || 'toi'
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
