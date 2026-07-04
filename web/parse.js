// Port of the Android parsing + grade logic to JS.
// Mirrors app/.../api/JsonParsing.kt and shared/.../domain/model/GradeSummary.kt.
// Instants are epoch-millis numbers (or null). Kept faithful, not clever.

const DATE_KEYS = [
  'date', 'createdAt', 'publishedAt', 'date_exam', 'dateExam',
  'date_examen', 'dateExamen', 'examDate', 'exam_date',
  'publishDate', 'publish_date', 'published', 'updatedAt',
  'update_date', 'date_note', 'dateNote', 'published_at', 'created_at'
]
const PROJECT_DATE_KEYS = [
  'deadline', 'dueDate', 'endDate', 'psp_limit_date', 'limit_date',
  'limitDate', 'date_limit', 'dateLimit', 'date', 'due_date', 'due',
  'update_date', 'date_limite', 'dateLimite'
]
const NOT_COUNTED_COEFFICIENT = -1
const NOT_COUNTED_REGEX = /^n\.?\s*c\.?$/i
const CC_SUBJECT_REGEX = /^(cc|contrôle continu)\s*\d*$/i

const CAMPUS_LOCATIONS = {
  NATION1: '242 rue du Faubourg Saint Antoine, 75012 Paris',
  NATION2: '220 rue du Faubourg Saint Antoine, 75012 Paris',
  VOLTAIRE1: '1 rue Bouvier, 75011 Paris',
  VOLTAIRE2: '20 rue Bouvier, 75011 Paris',
  ERARD: '19-21 rue Erard, 75011 Paris',
  BEAUGRENELLE: '35 quai André Citroen 75015 Paris',
  MONTSOURIS: '5 rue Lemaignan, 75014 Paris',
  MONTROUGE: '11 rue Camille Pelletan, 92120 Montrouge',
  JOURDAN: '6-10 bd Jourdan 75014 Paris',
  VAUGIRARD: '273-277 rue de Vaugirard, 75012 Paris',
  'MAIN-D-OR': '8‐14 Passage de la Main d’Or 75011 Paris',
  RANELAGH: '64-70 Rue du Ranelagh, 75016 Paris'
}
const CAMPUS_COLORS = {
  NATION1: '3', NATION2: '2', VOLTAIRE1: '5', VOLTAIRE2: '5', ERARD: '4',
  BEAUGRENELLE: '1', MONTSOURIS: '3', MONTROUGE: '6', JOURDAN: '7',
  VAUGIRARD: '9', 'MAIN-D-OR': '8', RANELAGH: '10'
}
const KNOWN_MODALITIES = new Set(['PRÉSENTIEL', 'PRESENTIEL', 'DISTANTIEL', 'HYBRIDE', 'E-LEARNING', 'ONLINE'])

// ---- low-level helpers ----
const isObj = v => v !== null && typeof v === 'object' && !Array.isArray(v)

function objectOrData(el) {
  if (!isObj(el)) return {}
  if (isObj(el.result)) return el.result
  if (isObj(el.data)) return el.data
  return el
}

function arrayOrNested(el, keys) {
  if (Array.isArray(el)) return el
  if (!isObj(el)) return []
  const allKeys = [...new Set([...keys, 'result', 'data', 'items', 'results'])]
  for (const key of allKeys) {
    const value = el[key]
    if (Array.isArray(value)) return value
    if (isObj(value)) {
      const nested = arrayOrNested(value, ['content', 'items', 'data', 'results'])
      if (nested.length) return nested
    }
  }
  const firstArray = Object.values(el).find(Array.isArray)
  return firstArray || []
}

function text(obj, ...keys) {
  if (!isObj(obj)) return null
  for (const key of keys) {
    const value = obj[key]
    if (value != null && typeof value !== 'object') {
      const s = String(value).trim()
      if (s) return s
    } else if (isObj(value)) {
      const nested = text(value, 'name', 'title', 'label', 'value')
      if (nested) return nested
    }
  }
  return null
}

function toNum(value) {
  if (value == null || typeof value === 'object') return null
  if (typeof value === 'number') return Number.isFinite(value) ? value : null
  const n = Number(String(value).replace(',', '.'))
  return Number.isFinite(n) ? n : null
}

function number(obj, ...keys) {
  if (!isObj(obj)) return null
  for (const key of keys) {
    const n = toNum(obj[key])
    if (n != null) return n
  }
  return null
}

function bool(obj, ...keys) {
  if (!isObj(obj)) return null
  for (const key of keys) {
    const v = obj[key]
    if (typeof v === 'boolean') return v
    switch (String(v).toLowerCase()) {
      case 'true': case 'yes': case '1': case 'justified': case 'justifiee': return true
      case 'false': case 'no': case '0': case 'unjustified': case 'injustifiee': return false
    }
  }
  return null
}

function array(obj, ...keys) {
  if (!isObj(obj)) return []
  for (const key of keys) {
    if (Array.isArray(obj[key])) return obj[key]
  }
  return []
}

function arrayText(obj, key, ...textKeys) {
  const parts = array(obj, key).map(it => text(it, ...textKeys)).filter(Boolean)
  return parts.length ? parts.join(', ') : null
}

function coefficientValue(obj) {
  const raw = (obj.coefficient ?? obj.coef)
  if (raw != null && NOT_COUNTED_REGEX.test(String(raw).trim())) return NOT_COUNTED_COEFFICIENT
  return number(obj, 'coefficient', 'coef')
}

function stableId(obj) {
  const s = JSON.stringify(obj) || String(Math.random())
  let h = 0
  for (let i = 0; i < s.length; i++) h = (Math.imul(31, h) + s.charCodeAt(i)) | 0
  return 'id' + (h >>> 0).toString(16)
}

// Returns epoch millis or null.
function parseInstant(value) {
  if (value == null || typeof value === 'object') return null
  const clean = String(value).trim().replace(/\s+/g, ' ')
  if (!clean) return null
  if (/^\d+$/.test(clean)) {
    const n = Number(clean)
    return n > 9999999999 ? n : n * 1000
  }
  let m = clean.match(/^(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2})h(\d{2})$/)
  if (m) return new Date(+m[3], +m[2] - 1, +m[1], +m[4], +m[5]).getTime()
  m = clean.match(/^(\d{2})\/(\d{2})\/(\d{4}) (\d{1,2}):(\d{2})$/)
  if (m) return new Date(+m[3], +m[2] - 1, +m[1], +m[4], +m[5]).getTime()
  m = clean.match(/^(\d{2})\/(\d{2})\/(\d{4})$/)
  if (m) return new Date(+m[3], +m[2] - 1, +m[1]).getTime()
  const t = Date.parse(clean)
  return Number.isNaN(t) ? null : t
}

function instant(obj, ...keys) {
  if (!isObj(obj)) return null
  for (const key of keys) {
    const v = parseInstant(obj[key])
    if (v != null) return v
  }
  return null
}

// ---- profile / years ----
export function parseProfile(json) {
  const root = objectOrData(json)
  const id = text(root, 'id', 'uid', 'puid', 'studentId', 'student_id') || 'profile'
  const firstName = text(root, 'firstName', 'firstname', 'prenom')
  const lastName = text(root, 'lastName', 'lastname', 'name', 'nom')
  const displayName = text(root, 'displayName', 'fullName', 'nomComplet') ||
    [firstName, lastName].filter(Boolean).join(' ') || text(root, 'name') || id
  return {
    id,
    displayName,
    email: text(root, 'email', 'mail'),
    school: text(root, 'school', 'campus', 'institution'),
    program: text(root, 'program', 'programme', 'formation', 'className'),
    academicYear: text(root, 'academicYear', 'year', 'annee'),
    avatarUrl: text(root, 'avatarUrl', 'avatar', 'picture')
  }
}

export function parseYears(json) {
  return arrayOrNested(json, ['years', 'data', 'items'])
    .map(el => (typeof el === 'object' ? text(el, 'year', 'id', 'value', 'academicYear', 'name') : (el != null ? String(el) : null)))
    .filter(v => v && v.trim())
    .filter((v, i, a) => a.indexOf(v) === i)
}

// ---- agenda ----
export function parseAgenda(json) {
  return arrayOrNested(json, ['agenda', 'events', 'items', 'data']).map(element => {
    const root = objectOrData(element)
    const discipline = isObj(root.discipline) ? root.discipline : null
    const startsAt = instant(root, 'startsAt', 'start', 'startDate', 'dateStart', 'begin', 'start_date')
    if (startsAt == null) return null
    const endsAt = instant(root, 'endsAt', 'end', 'endDate', 'dateEnd', 'end_date') ?? startsAt + 3600000

    const rooms = array(root, 'rooms')
    let campusKey = null
    let roomName = null
    if (rooms.length && isObj(rooms[0])) {
      roomName = text(rooms[0], 'name')
      campusKey = text(rooms[0], 'campus')
    }
    if (!roomName) roomName = text(root, 'room', 'classroom', 'salle') || arrayText(root, 'rooms', 'name')
    if (!campusKey) {
      const rootCampus = text(root, 'campus')
      if (rootCampus) campusKey = rootCampus
      else {
        const rootModality = text(root, 'modality', 'mode')
        if (rootModality && CAMPUS_LOCATIONS[rootModality.toUpperCase().trim()]) campusKey = rootModality
      }
    }

    let address = null
    let colorId = '11'
    if (campusKey) {
      const key = campusKey.toUpperCase().trim()
      if (CAMPUS_LOCATIONS[key]) {
        address = CAMPUS_LOCATIONS[key]
        colorId = CAMPUS_COLORS[key] || '11'
      } else if (!KNOWN_MODALITIES.has(key)) {
        address = campusKey
      }
    }

    return {
      id: text(root, 'id', 'eventId', 'uid', 'reservation_id') || stableId(root),
      title: text(root, 'title', 'name', 'courseName', 'matiere') || (discipline && text(discipline, 'name')) || '',
      startsAt,
      endsAt,
      room: roomName,
      teacher: text(root, 'teacher', 'intervenant', 'professor') || (discipline && text(discipline, 'teacher')),
      type: text(root, 'type', 'kind', 'prestation_type'),
      modality: text(root, 'modality', 'mode', 'campus'),
      courseId: text(root, 'courseId', 'rcId', 'rc_id', 'moduleId') || (discipline && text(discipline, 'rc_id')),
      address,
      colorId
    }
  }).filter(Boolean)
}

// ---- grades ----
function isToeicExcluded(g) {
  return (/toeic/i.test(g.courseName) || /toeic/i.test(g.subject)) && (g.value ?? 0) > 20
}
function makeGrade(root, { idSuffix = null, subject = null, value = null, scale = null, date = null, year = null } = {}) {
  const baseId = text(root, 'id', 'gradeId', 'uid', 'rc_id') || stableId(root)
  const rawPeriod = text(root, 'period', 'trimester_name', 'semester', 'trimester')
  let resolvedPeriod = rawPeriod
  if (rawPeriod && year && !rawPeriod.includes(year)) {
    const startYearNum = parseInt(year, 10)
    resolvedPeriod = Number.isFinite(startYearNum) ? `${startYearNum}-${startYearNum + 1} - ${rawPeriod}` : `${year} - ${rawPeriod}`
  }
  const g = {
    id: [baseId, idSuffix].filter(Boolean).join('-'),
    courseName: text(root, 'courseName', 'course', 'module', 'matiere') || '',
    subject: subject != null ? subject : (text(root, 'subject', 'title', 'name', 'evaluation', 'trimester_name') || ''),
    value,
    scale: scale ?? number(root, 'scale', 'outOf', 'bareme') ?? 20,
    coefficient: coefficientValue(root),
    average: number(root, 'average', 'moyenne', 'ccaverage'),
    date: date ?? instant(root, ...DATE_KEYS),
    period: resolvedPeriod
  }
  if (isToeicExcluded(g)) g.scale = 990
  return g
}

export function parseGrades(json, year = null) {
  const rootObj = isObj(json) ? json : null
  const componentArray = rootObj && Array.isArray(rootObj.grades) ? rootObj.grades : null
  const isSingleStructuredCourse = !!rootObj && ('exam' in rootObj ||
    (componentArray && componentArray.every(it => typeof it !== 'object')))
  const elements = isSingleStructuredCourse ? [json] : arrayOrNested(json, ['grades', 'items', 'data'])

  return elements.flatMap(element => {
    const root = objectOrData(element)
    if (!('grades' in root) && !('exam' in root) && !('course' in root)) {
      return [makeGrade(root, { value: number(root, 'grade', 'value', 'note'), year })]
    }
    const nested = array(root, 'grades')
    const ccGrades = nested.map(el => {
      let value
      if (typeof el !== 'object') value = toNum(el)
      else if (isObj(el)) value = number(el, 'value', 'grade', 'note')
      const dateVal = isObj(el) ? instant(el, ...DATE_KEYS) : null
      return value == null ? null : [value, dateVal]
    }).filter(Boolean)

    const examValue = number(root, 'exam', 'examen')
    const examDate = instant(root, 'date_exam', 'exam_date')
    const ccAverage = ccGrades.length ? ccGrades.reduce((s, p) => s + p[0], 0) / ccGrades.length : null
    const calculated = combineCcExam(ccAverage, examValue)
    const fallbackAvg = number(root, 'average', 'moyenne')
    const finalAverage = calculated ?? (fallbackAvg && fallbackAvg !== 0 ? fallbackAvg : null)
    const scale = number(root, 'scale', 'outOf', 'bareme')

    const result = [makeGrade(root, { subject: '', value: finalAverage, scale, date: instant(root, ...DATE_KEYS), year })]
    ccGrades.forEach((pair, i) => result.push(makeGrade(root, {
      idSuffix: `cc-${i}`, subject: `CC${i + 1}`, value: pair[0], scale, date: pair[1] ?? instant(root, ...DATE_KEYS), year
    })))
    if (examValue != null) result.push(makeGrade(root, {
      idSuffix: 'exam', subject: 'Examen', value: examValue, scale, date: examDate ?? instant(root, ...DATE_KEYS), year
    }))
    return result
  })
}

// ---- grade summary (weighted average / GPA) ----
export function combineCcExam(ccAverage, examValue) {
  if (ccAverage != null && examValue != null) return 0.5 * ccAverage + 0.5 * examValue
  if (ccAverage != null) return ccAverage
  if (examValue != null) return examValue
  return null
}

function isNotCounted(g) { return g.coefficient === NOT_COUNTED_COEFFICIENT }
export function isExcludedFromAverage(g) { return isToeicExcluded(g) || isNotCounted(g) }

export function toGradeSummary(grades) {
  const relevant = grades.filter(g => !isExcludedFromAverage(g))
  const graded = relevant.filter(g => g.value != null && g.scale != null && g.scale > 0)
  const weighted = graded.map(g => {
    const coef = g.coefficient != null && g.coefficient > 0 ? g.coefficient : null
    return {
      normalizedValue: (g.value / g.scale) * 20,
      coefficient: coef ?? 1,
      missingCoefficient: coef == null
    }
  })
  const missingCoefficientCount = weighted.filter(w => w.missingCoefficient).length
  const coefficientSum = weighted.reduce((s, w) => s + w.coefficient, 0)
  const weightedAverage = coefficientSum > 0
    ? weighted.reduce((s, w) => s + w.normalizedValue * w.coefficient, 0) / coefficientSum
    : null
  return {
    weightedAverage,
    gpa: weightedAverage != null ? Math.min(4, Math.max(0, weightedAverage / 20 * 4)) : null,
    gradedCount: graded.length,
    missingCoefficientCount,
    incomplete: missingCoefficientCount > 0 || graded.length < relevant.length
  }
}

// Simulation helpers (port of FeatureScreens.kt withSimulatedValues / withRecomputedMainGrades).
export function withSimulatedValues(grades, values) {
  return grades.map(g => (values[g.id] != null ? { ...g, value: values[g.id] } : g))
}

export function withRecomputedMainGrades(grades, overrideIds = new Set()) {
  const byCourse = new Map()
  for (const g of grades) {
    const k = `${g.courseName}|${g.period}`
    if (!byCourse.has(k)) byCourse.set(k, [])
    byCourse.get(k).push(g)
  }
  return grades.map(g => {
    if (g.id.includes('-cc-') || g.id.includes('-exam') || overrideIds.has(g.id)) return g
    const comps = byCourse.get(`${g.courseName}|${g.period}`) || []
    const ccValues = comps.filter(c => c.id.includes('-cc-')).map(c => c.value).filter(v => v != null)
    const examValue = (comps.find(c => c.id.includes('-exam')) || {}).value ?? null
    const ccAverage = ccValues.length ? ccValues.reduce((a, b) => a + b, 0) / ccValues.length : null
    const combined = combineCcExam(ccAverage, examValue)
    return combined != null ? { ...g, value: combined } : g
  })
}

// Components (CC + exam + main) of the same course+period as `grade`.
export function gradeComponents(grades, grade) {
  return grades.filter(g => g.courseName === grade.courseName && g.period === grade.period)
}

// Block grouping (port of FeatureScreens.kt blockKey / academicYearLabel).
const ACADEMIC_YEAR_RE = /\d{4}\s*-\s*\d{4}/
export function gradeAcademicYearLabel(g) {
  const m = ACADEMIC_YEAR_RE.exec(g.period || '')
  if (m) return m[0].replace(/\s/g, '')
  if (g.date == null) return ''
  const d = new Date(g.date)
  const startYear = (d.getUTCMonth() + 1) >= 9 ? d.getUTCFullYear() : d.getUTCFullYear() - 1
  return `${startYear}-${startYear + 1}`
}
export function gradeBlockKey(g) {
  return `${gradeAcademicYearLabel(g)}|${g.courseName}`
}

// ---- courses ----
export function parseCourses(json) {
  return arrayOrNested(json, ['courses', 'items', 'data']).map(el => {
    const root = objectOrData(el)
    const files = array(root, 'files', 'documents')
    const fc = files.length || number(root, 'fileCount', 'file_count', 'files_count', 'document_count', 'documents_count', 'nb_documents') || (bool(root, 'has_documents') === true ? 1 : 0)
    return {
      id: text(root, 'id', 'rcId', 'rc_id', 'courseId', 'uid') || stableId(root),
      name: text(root, 'name', 'title', 'courseName', 'course_name', 'matiere') || '',
      teacher: text(root, 'teacher', 'intervenant', 'professor'),
      year: text(root, 'year', 'academicYear'),
      period: text(root, 'period', 'trimester_name', 'semester', 'trimester'),
      syllabus: text(root, 'syllabus', 'description', 'summary'),
      fileCount: Math.trunc(fc) || 0
    }
  })
}

export function parseCourseSyllabus(json) {
  const arr = arrayOrNested(json, ['result', 'data'])
  const root = arr.length ? objectOrData(arr[0]) : objectOrData(json)
  const parts = [
    text(root, 'syllabus_name', 'course_name'),
    text(root, 'teaching_goals'),
    text(root, 'detail_plan'),
    text(root, 'skills') || arrayText(root, 'skills', 'comp_label', 'label', 'name'),
    text(root, 'prerequisite'),
    text(root, 'teaching_method'),
    text(root, 'evaluation_type') || arrayText(root, 'control_types', 'evaluation_label', 'label', 'name'),
    text(root, 'evaluation_criteria'),
    text(root, 'books_reference'),
    text(root, 'other_reference'),
    text(root, 'computing_tools'),
    arrayText(root, 'seance_details', 'title', 'name', 'description', 'detail', 'content')
  ].map(s => (s || '').trim()).filter(Boolean)
  return [...new Set(parts)].join('\n\n') || null
}

// ---- documents ----
function toDocument(root, parentTitle, parentYear, ownerId, groupId) {
  const title = text(root, 'title', 'name', 'label', 'pf_title', 'psf_name', 'psf_desc') || parentTitle || ''
  const ext = text(root, 'extension', 'psf_file_type')
  const fileName = text(root, 'fileName', 'filename', 'file', 'pf_file', 'psf_file', 'psf_name') || title
  return {
    id: text(root, 'id', 'documentId', 'document_id', 'oc_id', 'pf_id', 'psf_id', 'uid') || stableId(root),
    title,
    category: text(root, 'category', 'type'),
    year: text(root, 'year', 'academicYear') || parentYear || null,
    fileName: ext && !fileName.toLowerCase().endsWith(ext.toLowerCase()) ? `${fileName}.${ext.replace(/^\./, '')}` : fileName,
    downloadUrl: text(root, 'downloadUrl', 'url', 'href') || linkHref(root, 'url', 'download', 'file'),
    updatedAt: instant(root, 'updatedAt', 'last_update', 'update_date', 'pf_crea_date', 'psf_end_upload', 'psf_begin_upload', 'date', 'createdAt'),
    ownerId: ownerId || null,
    groupId: groupId || null
  }
}

export function parseDocuments(json, fallbackYear = null) {
  const root = objectOrData(json)
  const annual = array(root, 'annualDocuments')
  const docs = array(root, 'documents')
  if (annual.length || docs.length) {
    return [...annual, ...docs].map(d => toDocument(objectOrData(d), null, fallbackYear))
  }
  return arrayOrNested(json, ['items', 'data']).map(d => toDocument(objectOrData(d), null, fallbackYear))
}

// ---- directory ----
function directoryDisplayName(root) {
  return text(root, 'displayName', 'fullName', 'fullname', 'nomComplet') ||
    [text(root, 'civility', 'civilite'), text(root, 'firstName', 'firstname', 'prenom'), text(root, 'lastName', 'lastname', 'name', 'nom')]
      .filter(Boolean).join(' ').trim() || text(root, 'name', 'email', 'mail')
}

export function parseDirectory(json, role, year = null) {
  return arrayOrNested(json, ['students', 'teachers', 'items', 'data']).map(el => {
    const root = objectOrData(el)
    const rawId = text(root, 'id', 'uid', 'puid', 'student_id', 'studentId', 'teacher_id') || stableId(root)
    return {
      id: `${role}:${year}:${rawId}`,
      displayName: directoryDisplayName(root) || rawId,
      email: text(root, 'email', 'mail'),
      role,
      year: text(root, 'year', 'academicYear') || year,
      groupName: text(root, 'groupName', 'className', 'promotion', 'student_group_name')
    }
  })
}

// ---- events ----
export function parseEvents(json) {
  return arrayOrNested(json, ['events', 'items', 'data']).map(el => {
    const root = objectOrData(el)
    const rawDesc = text(root, 'description', 'summary', 'text')
    return {
      id: text(root, 'event_id', 'id', 'uid') || stableId(root),
      title: text(root, 'event_title', 'title', 'name', 'label') || '',
      type: text(root, 'event_type', 'type'),
      location: text(root, 'location', 'place'),
      organizer: text(root, 'organizer'),
      description: rawDesc && rawDesc.includes('<') ? htmlToPlainText(rawDesc) : rawDesc,
      date: instant(root, 'event_date', 'date'),
      subscriptionStart: instant(root, 'start_subscription_date'),
      subscriptionEnd: instant(root, 'end_subscription_date'),
      subscribed: bool(root, 'is_participant_subscribed', 'subscribed') === true
    }
  })
}

function linkHref(root, ...rels) {
  const links = array(root, 'links').filter(isObj)
  if (rels.length) {
    const m = links.find(l => rels.includes(text(l, 'rel')))
    if (m) return text(m, 'href', 'url')
  }
  const first = links.map(l => text(l, 'href', 'url')).find(Boolean)
  return first || null
}

// Shared project/practical group + files parser.
function parseGroups(root, currentUserId) {
  const userGroupIds = new Set(array(root, 'project_group_logs').map(objectOrData)
    .filter(log => currentUserId && text(log, 'user_id', 'uid', 'u_id') === currentUserId)
    .map(log => text(log, 'pgr_id', 'project_group_id', 'group_id')).filter(Boolean))
  return array(root, 'groups').map(gr => {
    const g = objectOrData(gr)
    const groupId = text(g, 'project_group_id', 'pgr_id', 'group_id', 'id') || stableId(g)
    const students = array(g, 'project_group_students', 'students').map(s => directoryDisplayName(objectOrData(s))).filter(Boolean)
    const mine = userGroupIds.has(groupId) ||
      array(g, 'project_group_students', 'students').some(s => {
        const so = objectOrData(s)
        return currentUserId && text(so, 'user_id', 'uid', 'u_id', 'id', 'studentId', 'student_id') === currentUserId
      })
    return { id: groupId, name: text(g, 'group_name', 'name', 'label') || groupId, students, isMine: mine }
  })
}

function parseProjectFiles(root, parentTitle, year, ownerId) {
  const direct = array(root, 'files', 'project_files', 'documents', 'deliverables').map(f => {
    const d = toDocument(objectOrData(f), parentTitle, year, ownerId)
    return { ...d, downloadUrl: d.downloadUrl || `me/projectFiles/${d.id}` }
  })
  const stepFiles = array(root, 'steps', 'projectSteps').flatMap(step => {
    const s = objectOrData(step)
    return array(s, 'files').map(f => {
      const fo = objectOrData(f)
      const d = toDocument(fo, text(s, 'title', 'name', 'psp_desc', 'psp_type'), year, ownerId, text(fo, 'project_group_id', 'pgr_id', 'group_id'))
      return { ...d, downloadUrl: d.downloadUrl || `me/projectStepFiles/${d.id}` }
    })
  })
  return [...direct, ...stepFiles]
}

export function parsePracticals(json, fallbackYear = null, currentUserId = null) {
  return arrayOrNested(json, ['practicals', 'items', 'data']).map(el => {
    const root = objectOrData(el)
    const steps = array(root, 'steps', 'projectSteps').map(step => {
      const s = objectOrData(step)
      return { id: text(s, 'id', 'stepId', 'psp_id', 'uid') || stableId(s), title: text(s, 'title', 'name', 'psp_desc', 'psp_type') || '', deadline: instant(s, ...PROJECT_DATE_KEYS), status: text(s, 'status', 'state') }
    })
    const id = text(root, 'id', 'practicalId', 'project_id', 'uid') || stableId(root)
    return {
      id,
      name: text(root, 'name', 'title') || '',
      courseName: text(root, 'courseName', 'course_name', 'course', 'module'),
      startsAt: instant(root, 'startsAt', 'start', 'startDate', 'dateStart', 'project_create_date'),
      endsAt: instant(root, 'endsAt', 'end', 'endDate', 'dateEnd'),
      room: text(root, 'room', 'classroom', 'salle'),
      status: text(root, 'status', 'state'),
      year: text(root, 'year', 'academicYear') || fallbackYear,
      steps,
      groups: parseGroups(root, currentUserId),
      documents: parseProjectFiles(root, text(root, 'name', 'title'), text(root, 'year', 'academicYear') || fallbackYear, id)
    }
  })
}

export function mainGrades(grades) {
  const structuredKeys = new Set(
    grades.filter(g => g.subject.trim() === '' && !g.id.includes('-cc-') && !g.id.includes('-exam'))
      .map(g => `${g.courseName}|${g.period}`)
  )
  return grades.filter(g =>
    !g.id.includes('-cc-') &&
    !g.id.includes('-exam') &&
    !(structuredKeys.has(`${g.courseName}|${g.period}`) &&
      (CC_SUBJECT_REGEX.test(g.subject) || g.subject.trim().toLowerCase() === 'examen'))
  )
}

// ---- absences ----
export function parseAbsences(json, year = null) {
  return arrayOrNested(json, ['absences', 'items', 'data']).map(element => {
    const root = objectOrData(element)
    const startsAt = instant(root, 'startsAt', 'start', 'startDate', 'dateStart', 'date')
    if (startsAt == null) return null
    const endsAt = instant(root, 'endsAt', 'end', 'endDate', 'dateEnd') ?? startsAt + 3600000
    const d = new Date(startsAt)
    const month = d.getUTCMonth() + 1
    const startYearNum = number(root, 'year', 'academicYear') ?? (month >= 9 ? d.getUTCFullYear() : d.getUTCFullYear() - 1)
    const yearLabel = `${startYearNum}-${startYearNum + 1}`
    let semesterNumber = (String(text(root, 'trimester_name', 'semester') || '').match(/\d/) || [])[0]
    semesterNumber = semesterNumber ? parseInt(semesterNumber, 10) : (month >= 2 && month <= 8 ? 2 : 1)
    return {
      id: text(root, 'id', 'absenceId', 'uid') || stableId(root),
      courseName: text(root, 'courseName', 'course_name', 'course', 'module', 'matiere') || '',
      startsAt,
      endsAt,
      justified: bool(root, 'justified', 'isJustified', 'justifiee') === true,
      status: text(root, 'status', 'state', 'etat', 'type'),
      reason: text(root, 'reason', 'motif'),
      period: `${yearLabel} - Semestre ${semesterNumber}`
    }
  }).filter(Boolean)
}

// ---- projects ----
export function parseProjects(json, fallbackYear = null, currentUserId = null) {
  const arr = arrayOrNested(json, ['projects', 'items', 'data'])
  const elements = arr.length ? arr : (isObj(objectOrData(json)) && Object.keys(objectOrData(json)).length ? [objectOrData(json)] : [])
  return elements.map(element => {
    const root = objectOrData(element)
    const steps = array(root, 'steps', 'projectSteps').map(step => {
      const s = objectOrData(step)
      return {
        id: text(s, 'id', 'stepId', 'psp_id', 'uid') || stableId(s),
        title: text(s, 'title', 'name', 'psp_desc', 'psp_type') || '',
        deadline: instant(s, ...PROJECT_DATE_KEYS),
        status: text(s, 'status', 'state')
      }
    })
    const stepFileCount = array(root, 'steps', 'projectSteps').reduce((n, s) => n + array(objectOrData(s), 'files').length, 0)
    const id = text(root, 'id', 'projectId', 'project_id', 'uid') || stableId(root)
    const year = text(root, 'year', 'academicYear') || fallbackYear
    const groups = parseGroups(root, currentUserId)
    return {
      id,
      name: text(root, 'name', 'title') || '',
      courseName: text(root, 'courseName', 'course_name', 'course', 'module'),
      groupName: text(root, 'groupName', 'group', 'projectGroup') || groups.find(g => g.isMine)?.name || arrayText(root, 'groups', 'group_name', 'name'),
      status: text(root, 'status', 'state'),
      deadline: instant(root, ...PROJECT_DATE_KEYS) ?? steps.map(s => s.deadline).filter(v => v != null).sort((a, b) => b - a)[0] ?? null,
      steps,
      fileCount: array(root, 'files', 'project_files', 'documents', 'deliverables').length + stepFileCount,
      year,
      courseId: text(root, 'rc_id', 'rcId', 'courseId'),
      groups,
      groupMode: text(root, 'project_type_group', 'type_group', 'groupMode'),
      maxStudents: number(root, 'project_max_student_group', 'max_student_group', 'maxStudents'),
      documents: parseProjectFiles(root, text(root, 'name', 'title'), year, id)
    }
  })
}

// ---- news ----
export function parseNews(json) {
  const arr = arrayOrNested(json, ['news', 'banners', 'content', 'items', 'data'])
  return arr.map(element => {
    const root = objectOrData(element)
    const html = text(root, 'html', 'content', 'body')
    const preview = text(root, 'summary', 'text', 'description', 'value') || (html ? htmlToPlainText(html) : null)
    return {
      id: text(root, 'id', 'newsId', 'ne_id', 'partner_id', 'ss_id', 'type', 'uid') || stableId(root),
      title: text(root, 'title', 'name', 'label') || '',
      body: preview,
      html: html && html.includes('<') ? html : null,
      publishedAt: instant(root, 'publishedAt', 'date', 'createdAt', 'update_date', 'appointment_start',
        'start_date', 'creation_date', 'publication_date', 'publish_date', 'begin_date'),
      imageUrl: text(root, 'image', 'photo', 'picture', 'banner', 'imageUrl')
    }
  })
}

function htmlToPlainText(s) {
  return s
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/(p|div|h[1-6]|li)>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>')
    .replace(/&#(\d+);/g, (_, n) => String.fromCodePoint(+n))
    .replace(/[ \t]+/g, ' ').replace(/ *\n */g, '\n').replace(/\n{3,}/g, '\n\n')
    .trim()
}
