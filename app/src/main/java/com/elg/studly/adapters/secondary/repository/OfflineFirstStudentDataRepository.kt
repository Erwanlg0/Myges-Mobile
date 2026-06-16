package com.elg.studly.adapters.secondary.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.elg.studly.adapters.secondary.pdf.PdfGenerator
import com.elg.studly.adapters.secondary.api.MyGesApiService
import com.elg.studly.adapters.secondary.api.toAbsences
import com.elg.studly.adapters.secondary.api.toAgendaEvents
import com.elg.studly.adapters.secondary.api.toClassIds
import com.elg.studly.adapters.secondary.api.toCourseSyllabus
import com.elg.studly.adapters.secondary.api.toCourses
import com.elg.studly.adapters.secondary.api.toDirectoryPeople
import com.elg.studly.adapters.secondary.api.toDocuments
import com.elg.studly.adapters.secondary.api.toGrades
import com.elg.studly.adapters.secondary.api.toNews
import com.elg.studly.adapters.secondary.api.toNextProjectStepProjects
import com.elg.studly.adapters.secondary.api.toPracticals
import com.elg.studly.adapters.secondary.api.toProfile
import com.elg.studly.adapters.secondary.api.toPracticalDocuments
import com.elg.studly.adapters.secondary.api.toProjectDocuments
import com.elg.studly.adapters.secondary.api.toProjects
import com.elg.studly.adapters.secondary.api.toYears
import com.elg.studly.adapters.secondary.storage.StudentDao
import com.elg.studly.adapters.secondary.storage.toDomain
import com.elg.studly.adapters.secondary.storage.toEntity
import com.elg.studly.adapters.secondary.storage.toGroupEntities
import com.elg.studly.adapters.secondary.storage.toStepEntities
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.DirectoryRole
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.ZoneOffset
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class OfflineFirstStudentDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: MyGesApiService,
    private val dao: StudentDao,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : StudentDataRepository {
    override fun observeDashboard(): Flow<DashboardSummary> {
        return combine(
            dao.observeProfile(),
            observeAgenda(),
            observeGrades(),
            observeAbsences(),
            observeProjects()
        ) { profile, agenda, grades, absences, projects ->
            DashboardLocalData(profile, agenda, grades, absences, projects)
        }.combine(settingsRepository.settings) { localData, settings ->
            val now = Instant.now()
            DashboardSummary(
                profile = localData.profile?.toDomain(),
                nextEvent = localData.agenda.firstOrNull { it.endsAt.isAfter(now) },
                latestGrades = run {
                    val allGrades = localData.grades.filter { it.value != null }
                    val structuredMainKeys = allGrades
                        .filter { it.subject.isBlank() && !it.id.contains("-cc-") && !it.id.contains("-exam") }
                        .map { it.courseName to it.period }
                        .toSet()
                    val ccRegex = Regex("^(cc|contrôle continu)\\s*\\d*$", RegexOption.IGNORE_CASE)
                    allGrades.filter { grade ->
                        !grade.id.contains("-cc-") &&
                        !grade.id.contains("-exam") &&
                        !((grade.courseName to grade.period) in structuredMainKeys &&
                            (grade.subject.matches(ccRegex) ||
                             grade.subject.trim().equals("examen", ignoreCase = true)))
                    }.sortedWith(compareByDescending<Grade> { it.date }.thenByDescending { it.id }).take(3)
                },
                recentAbsences = localData.absences.sortedByDescending { it.startsAt }.take(1),
                dueProjects = localData.projects
                    .filter { project ->
                        project.name.isNotBlank() && (
                            project.deadline?.isAfter(now) == true ||
                            project.steps.any { it.deadline?.isAfter(now) == true }
                        )
                    }
                    .sortedBy { project ->
                        listOfNotNull(
                            project.deadline?.takeIf { it.isAfter(now) },
                            project.steps.mapNotNull { it.deadline }.filter { it.isAfter(now) }.minOrNull()
                        ).minOrNull()
                    }
                    .take(3),
                lastSyncAt = settings.lastSyncAt
            )
        }
    }

    override fun observeAgenda(): Flow<List<AgendaEvent>> {
        return dao.observeAgenda().map { events -> events.map { it.toDomain() } }
    }

    override fun observeGrades(): Flow<List<Grade>> {
        return dao.observeGrades().map { grades -> grades.map { it.toDomain() } }
    }

    override fun observeAbsences(): Flow<List<Absence>> {
        return dao.observeAbsences().map { absences -> absences.map { it.toDomain() } }
    }

    override fun observeCourses(): Flow<List<Course>> {
        return dao.observeCourses().map { courses -> courses.map { it.toDomain() } }
    }

    override fun observeProjects(): Flow<List<Project>> {
        return combine(dao.observeProjects(), dao.observeProjectSteps(), dao.observeProjectGroups()) { projects, steps, groups ->
            val stepsByProject = steps.groupBy { it.projectId }
            val groupsByProject = groups.groupBy { it.projectId }
            projects.map { project -> project.toDomain(stepsByProject[project.id].orEmpty(), groupsByProject[project.id].orEmpty()) }
        }
    }

    override fun observePracticals(): Flow<List<Practical>> {
        return combine(dao.observePracticals(), dao.observeProjectSteps(), dao.observeProjectGroups()) { practicals, steps, groups ->
            val stepsByPractical = steps.groupBy { it.projectId }
            val groupsByPractical = groups.groupBy { it.projectId }
            practicals.map { it.toDomain(stepsByPractical[it.id].orEmpty(), groupsByPractical[it.id].orEmpty()) }
        }
    }

    override fun observeDocuments(): Flow<List<AcademicDocument>> {
        return dao.observeDocuments().map { documents -> documents.map { it.toDomain() } }
    }

    override fun observeDirectory(): Flow<List<DirectoryPerson>> {
        return dao.observeDirectory().map { people -> people.map { it.toDomain() } }
    }

    override fun observeNews(): Flow<List<NewsItem>> {
        return dao.observeNews().map { news -> news.map { it.toDomain() } }
    }

    override suspend fun syncAll() {
        withContext(Dispatchers.IO) {
            try {
                purgeExpiredDocumentCache(File(context.cacheDir, DOCUMENT_CACHE))
                val profile = api.profile().toProfile()
                val activeYears = (api.years()?.toYears().orEmpty() + 
                        runCatching { api.trimesterYears()?.toYears().orEmpty() }.getOrDefault(emptyList()) + 
                        listOfNotNull(profile.academicYear)
                    )
                    .flatMap { yearStr ->
                        Regex("\\d{4}").findAll(yearStr).map { it.value }
                    }
                    .distinct()
                    .sortedDescending()
                    .ifEmpty { listOf(Year.now().value.toString()) }
                
                val updatedProfile = profile.copy(
                    academicYear = activeYears.sorted().joinToString(", ")
                )

                val isFirstSync = settingsRepository.settings.first().lastSyncAt == null || dao.agendaIds().isEmpty()
                val agendaWindow = if (isFirstSync) {
                    AgendaWindow.firstSync()
                } else {
                    AgendaWindow.subsequentSync()
                }
                val agenda = api.agenda(
                    start = agendaWindow.start,
                    end = agendaWindow.end
                )?.toAgendaEvents().orEmpty()
                val nextProjectStepProjects = runCatching {
                    api.nextProjectSteps()?.toNextProjectStepProjects().orEmpty()
                }.getOrDefault(emptyList())
                val yearData = fetchAllYearsData(activeYears, updatedProfile.id)
                    .withNextProjectSteps(nextProjectStepProjects)
                val news = runCatching { api.minimumVersion()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
                    api.news()?.toNews().orEmpty() +
                    runCatching { api.newsBanners()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
                    runCatching { api.partners()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
                    runCatching { api.speedMeetingAppointments()?.toNews().orEmpty() }.getOrDefault(emptyList())
                val previousIds = SyncedIds(
                    agenda = dao.agendaIds().toSet(),
                    grades = dao.gradeIds().toSet(),
                    absences = dao.absenceIds().toSet(),
                    projects = dao.projectIds().toSet(),
                    documents = dao.documentIds().toSet()
                )
                dao.replaceSyncedData(
                    profile = updatedProfile.toEntity(),
                    agenda = agenda.map { it.toEntity() },
                    grades = yearData.grades.map { it.toEntity() },
                    absences = yearData.absences.map { it.toEntity() },
                    courses = yearData.courses.map { it.toEntity() },
                    projects = yearData.projects.map { it.toEntity() },
                    projectGroups = yearData.projects.flatMap { it.toGroupEntities() } + yearData.practicals.flatMap { it.toGroupEntities() },
                    projectSteps = yearData.projects.flatMap { it.toStepEntities() } + yearData.practicals.flatMap { it.toStepEntities() },
                    practicals = yearData.practicals.map { it.toEntity() },
                    documents = yearData.documents.map { it.toEntity() },
                    directoryPeople = yearData.directory.map { it.toEntity() },
                    news = news.distinctBy { it.id }.map { it.toEntity() }
                )
                notifyAboutChanges(previousIds, agenda, yearData.grades, yearData.absences, yearData.projects, yearData.documents)
            } catch (throwable: Throwable) {
                throw throwable.toRepositoryException()
            }
        }
    }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
            File(context.cacheDir, DOCUMENT_CACHE).deleteRecursively()
        }
    }

    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val directory = File(context.cacheDir, DOCUMENT_CACHE).apply { mkdirs() }
                purgeExpiredDocumentCache(directory)
                val inlineContent = document.inlineContent
                val target = if (inlineContent != null) {
                    val file = File(directory, document.fileName.sanitizedFileName())
                    onProgress(0f)
                    if (document.mimeType == "application/pdf") {
                        file.outputStream().use {
                            PdfGenerator.generatePdfFromText(inlineContent, document.title, it)
                        }
                    } else {
                        file.writeText(inlineContent, StandardCharsets.UTF_8)
                    }
                    onProgress(1f)
                    file
                } else {
                    val remoteUrl = document.downloadUrl ?: "me/annualDocuments/${document.id}"
                    val response = api.download(remoteUrl)
                    if (!response.isSuccessful) throw HttpException(response)
                    val body = response.body() ?: throw AppException(AppError.EmptyResponse)
                    val fileName = response.headers()["Content-Disposition"]?.contentDispositionFileName()
                        ?: document.fileName.withExtension(body.contentType())
                    val file = File(directory, fileName.sanitizedFileName())
                    val contentLength = body.contentLength()
                    var copied = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    onProgress(0f.takeIf { contentLength > 0 })
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                copied += read
                                onProgress(if (contentLength > 0) (copied.toFloat() / contentLength).coerceIn(0f, 1f) else null)
                            }
                        }
                    }
                    onProgress(1f)
                    file
                }
                target.setLastModified(Instant.now().toEpochMilli())
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target)
            } catch (throwable: Throwable) {
                throw throwable.toRepositoryException()
            }
        }
    }

    private fun Throwable.toRepositoryException(): AppException {
        return when (this) {
            is AppException -> this
            is HttpException -> if (code() == 401 || code() == 403) {
                AppException(AppError.Unauthorized)
            } else {
                val errorBody = runCatching { response()?.errorBody()?.string() }.getOrNull()
                val parsedMessage = errorBody?.let { body ->
                    runCatching {
                        val json = Json.parseToJsonElement(body).jsonObject
                        json["message"]?.jsonPrimitive?.content
                            ?: json["error"]?.jsonPrimitive?.content
                            ?: json["detail"]?.jsonPrimitive?.content
                            ?: body.take(100)
                    }.getOrElse {
                        body.take(100)
                    }
                }?.takeIf { it.isNotBlank() } ?: message()
                AppException(AppError.Remote(code(), parsedMessage))
            }
            is IOException -> AppException(AppError.Network)
            else -> AppException(AppError.Unexpected(message))
        }
    }

    private fun String.sanitizedFileName(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "document" }
    }

    private fun String.withExtension(contentType: MediaType?): String {
        if (substringAfterLast('.', missingDelimiterValue = "").isNotBlank()) return this
        return contentType?.toFileExtension()?.let { "$this.$it" } ?: this
    }

    private fun MediaType.toFileExtension(): String? {
        return when (toString().substringBefore(';').trim().lowercase()) {
            "application/pdf" -> "pdf"
            "application/zip" -> "zip"
            "text/plain" -> "txt"
            "text/markdown" -> "md"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            else -> null
        }
    }

    private fun String.contentDispositionFileName(): String? {
        return split(';')
            .map { it.trim() }
            .firstNotNullOfOrNull { part ->
                when {
                    part.startsWith("filename*=", ignoreCase = true) -> part.substringAfter('=')
                        .substringAfter("''")
                        .trim('"')
                        .let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
                        .takeIf { it.isNotBlank() }
                    part.startsWith("filename=", ignoreCase = true) -> part.substringAfter('=')
                        .trim('"')
                        .takeIf { it.isNotBlank() }
                    else -> null
                }
            }
    }

    private suspend fun fetchAllYearsData(years: List<String>, currentUserId: String): YearData {
        val allCourses = mutableListOf<Course>()
        val allGrades = mutableListOf<Grade>()
        val allAbsences = mutableListOf<Absence>()
        val allDocuments = mutableListOf<AcademicDocument>()
        val allProjects = mutableListOf<Project>()
        val allPracticals = mutableListOf<Practical>()
        val allDirectory = mutableListOf<DirectoryPerson>()
        val fetchedCourseDocIds = mutableSetOf<String>()

        years.forEach { year ->
            val courses = runCatching { api.courses(year)?.toCourses().orEmpty() }.getOrDefault(emptyList())
                .withRemoteSyllabus()
            val grades = runCatching { api.grades(year)?.toGrades(year).orEmpty() }.getOrDefault(emptyList())
            val projectsJson = runCatching { api.projects(year) }.getOrNull()
            
            val newCourses = courses.filter { it.id !in fetchedCourseDocIds }
            fetchedCourseDocIds.addAll(newCourses.map { it.id })
            val courseProjectPayloads = newCourses.mapNotNull { course ->
                runCatching { api.courseProjects(course.id) }.getOrNull()
            }
            val projectPayloads = listOfNotNull(projectsJson) + courseProjectPayloads
            val projects = projectPayloads.flatMap { it.toProjects(currentUserId, year) }.mergeProjects()
            
            val practicalsJson = runCatching { api.practicals(year) }.getOrNull()
            val coursePracticalPayloads = newCourses.mapNotNull { course ->
                runCatching { api.coursePracticals(course.id) }.getOrNull()
            }
            val practicalPayloads = listOfNotNull(practicalsJson) + coursePracticalPayloads
            val practicals = practicalPayloads.flatMap { it.toPracticals(currentUserId, year) }.distinctBy { it.id }

            val documents = runCatching { api.annualDocuments(year)?.toDocuments(year).orEmpty() }.getOrDefault(emptyList()) +
                courseDocuments(newCourses) +
                projectPayloads.flatMap { it.toProjectDocuments(year) } +
                practicalPayloads.flatMap { it.toPracticalDocuments(year) } +
                syllabusDocuments(courses)
                
            val availablePeriods = (grades.mapNotNull { it.period } + courses.mapNotNull { it.period })
                .filter { it.isNotBlank() && it.contains(Regex("\\d{4}")) }
                .distinct()
            val absences = runCatching { api.absences(year)?.toAbsences(year, availablePeriods).orEmpty() }.getOrDefault(emptyList())
            val directory = directoryPeople(year)

            allCourses.addAll(courses)
            allGrades.addAll(grades)
            allAbsences.addAll(absences)
            allDocuments.addAll(documents)
            allProjects.addAll(projects)
            allPracticals.addAll(practicals)
            allDirectory.addAll(directory)
        }

        return YearData(
            courses = allCourses.distinctBy { it.id },
            grades = allGrades.distinctBy { it.id },
            absences = allAbsences.distinctBy { it.id },
            documents = allDocuments.distinctBy { it.id },
            projects = allProjects.mergeProjects(),
            practicals = allPracticals.distinctBy { it.id },
            directory = allDirectory.distinctBy { it.id }
        )
    }



    private suspend fun YearData.withCachedOutsideFetchedYears(): YearData {
        val refreshedYears = courses.mapNotNull { it.year }.toSet() +
            grades.mapNotNull { it.academicYearStart() }.toSet() +
            absences.mapNotNull { it.academicYearStart() }.toSet() +
            documents.mapNotNull { it.year }.toSet()
        val cachedSteps = dao.projectSteps().groupBy { it.projectId }
        val cachedGroups = dao.projectGroups().groupBy { it.projectId }
        if (refreshedYears.isEmpty()) {
            return copy(
                courses = (courses + dao.courses().map { it.toDomain() }).distinctBy { it.id },
                grades = (grades + dao.grades().map { it.toDomain() }).distinctBy { it.id },
                absences = (absences + dao.absences().map { it.toDomain() }).distinctBy { it.id },
                documents = (documents + dao.documents().map { it.toDomain() }).distinctBy { it.id },
                projects = (projects + dao.projects().map { it.toDomain(cachedSteps[it.id].orEmpty(), cachedGroups[it.id].orEmpty()) }).distinctBy { it.id },
                practicals = (practicals + dao.practicals().map { it.toDomain(cachedSteps[it.id].orEmpty(), cachedGroups[it.id].orEmpty()) }).distinctBy { it.id }
            )
        }
        return copy(
            courses = (courses + dao.courses().map { it.toDomain() }.filter { it.year !in refreshedYears }).distinctBy { it.id },
            grades = (grades + dao.grades().map { it.toDomain() }.filter { it.academicYearStart() !in refreshedYears }).distinctBy { it.id },
            absences = (absences + dao.absences().map { it.toDomain() }.filter { it.academicYearStart() !in refreshedYears }).distinctBy { it.id },
            documents = (documents + dao.documents().map { it.toDomain() }.filter { it.year !in refreshedYears }).distinctBy { it.id },
            projects = (projects + dao.projects().map { it.toDomain(cachedSteps[it.id].orEmpty(), cachedGroups[it.id].orEmpty()) }).distinctBy { it.id },
            practicals = (practicals + dao.practicals().map { it.toDomain(cachedSteps[it.id].orEmpty(), cachedGroups[it.id].orEmpty()) }).distinctBy { it.id }
        )
    }

    private suspend fun courseDocuments(courses: List<Course>): List<AcademicDocument> {
        return courses.filter { it.fileCount > 0 }
            .flatMap { course ->
                runCatching {
                    api.courseFiles(course.id)
                        ?.toDocuments()
                        .orEmpty()
                        .map { document ->
                            if (document.downloadUrl != null) {
                                val originalUrl = document.downloadUrl
                                val separator = if (originalUrl.contains("?")) "&" else "?"
                                document.copy(downloadUrl = "$originalUrl${separator}courseId=${course.id}")
                            } else {
                                document.copy(downloadUrl = "me/${course.id}/files/${document.id}")
                            }
                        }
                }.getOrDefault(emptyList())
            }
    }

    private fun syllabusDocuments(courses: List<Course>): List<AcademicDocument> {
        return courses.mapNotNull { course ->
            val syllabus = course.syllabus?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            AcademicDocument(
                id = "syllabus-${course.id}",
                title = "${course.name} syllabus",
                category = "Syllabus",
                year = course.year,
                mimeType = "application/pdf",
                fileName = "${course.name}_syllabus.pdf",
                downloadUrl = null,
                updatedAt = null,
                ownerId = course.id,
                inlineContent = syllabus
            )
        }
    }

    private suspend fun directoryPeople(year: String): List<DirectoryPerson> {
        val teachers = runCatching { api.teachers(year)?.toDirectoryPeople(DirectoryRole.Teacher, year).orEmpty() }
            .getOrDefault(emptyList())
        val students = runCatching { api.students(year)?.toDirectoryPeople(DirectoryRole.Student, year).orEmpty() }
            .getOrDefault(emptyList())
        val classStudents = runCatching { api.classes(year)?.toClassIds().orEmpty() }
            .getOrDefault(emptyList())
            .flatMap { classId ->
                runCatching { api.classStudents(classId)?.toDirectoryPeople(DirectoryRole.Student, year).orEmpty() }
                    .recoverCatching { api.classStudents(classId, year)?.toDirectoryPeople(DirectoryRole.Student, year).orEmpty() }
                    .getOrDefault(emptyList())
            }
        return (teachers + students + classStudents).distinctBy { it.id }
    }

    private fun List<Project>.mergeProjects(): List<Project> {
        return groupBy { it.id }.values.map { projects ->
            projects.reduce { current, next ->
                current.copy(
                    name = current.name.ifBlank { next.name },
                    courseName = current.courseName ?: next.courseName,
                    groupName = current.groupName ?: next.groupName,
                    status = current.status ?: next.status,
                    deadline = listOfNotNull(current.deadline, next.deadline).minOrNull(),
                    steps = (current.steps + next.steps).distinctBy { it.id },
                    fileCount = max(current.fileCount, next.fileCount),
                    year = current.year ?: next.year,
                    courseId = current.courseId ?: next.courseId,
                    groups = (current.groups + next.groups).distinctBy { it.id }
                )
            }
        }
    }

    private suspend fun List<Course>.withRemoteSyllabus(): List<Course> {
        return map { course ->
            if (!course.syllabus.isNullOrBlank()) {
                course
            } else {
                val syllabus = runCatching { api.syllabus(course.id)?.toCourseSyllabus() }.getOrNull()
                if (syllabus.isNullOrBlank()) course else course.copy(syllabus = syllabus)
            }
        }
    }

    private suspend fun notifyAboutChanges(
        previousIds: SyncedIds,
        agenda: List<AgendaEvent>,
        grades: List<Grade>,
        absences: List<Absence>,
        projects: List<Project>,
        documents: List<AcademicDocument>
    ) {
        val settings = settingsRepository.settings.first()
        if (settings.notifications.agenda && previousIds.agenda.isNotEmpty()) {
            agenda.filter { it.id !in previousIds.agenda }.forEach { notificationScheduler.showAgendaChange(it) }
        }
        if (settings.notifications.grades && previousIds.grades.isNotEmpty()) {
            grades.filter { it.id !in previousIds.grades }.forEach { notificationScheduler.showNewGrade(it) }
        }
        if (settings.notifications.absences && previousIds.absences.isNotEmpty()) {
            absences.filter { it.id !in previousIds.absences }.forEach { notificationScheduler.showNewAbsence(it) }
        }
        if (settings.notifications.projects && previousIds.projects.isNotEmpty()) {
            projects.filter { it.id !in previousIds.projects }.forEach { notificationScheduler.showProjectDeadline(it) }
        }
        if (settings.notifications.documents && previousIds.documents.isNotEmpty()) {
            documents.filter { it.id !in previousIds.documents }.forEach { notificationScheduler.showNewDocument(it) }
        }
    }

    private companion object {
        const val DOCUMENT_CACHE = "documents"
    }
}

private data class SyncedIds(
    val agenda: Set<String>,
    val grades: Set<String>,
    val absences: Set<String>,
    val projects: Set<String>,
    val documents: Set<String>
)

private data class YearData(
    val courses: List<Course>,
    val grades: List<Grade>,
    val absences: List<Absence>,
    val documents: List<AcademicDocument>,
    val projects: List<Project>,
    val practicals: List<Practical>,
    val directory: List<DirectoryPerson>
) {
    fun hasAcademicData(): Boolean {
        return courses.isNotEmpty() ||
            grades.isNotEmpty() ||
            absences.isNotEmpty() ||
            documents.isNotEmpty() ||
            projects.isNotEmpty() ||
            practicals.isNotEmpty() ||
            directory.isNotEmpty()
    }

    fun withNextProjectSteps(nextProjectStepProjects: List<Project>): YearData {
        if (nextProjectStepProjects.isEmpty()) return this
        val upcomingByProjectId = nextProjectStepProjects.associateBy { it.id }
        val mergedProjects = projects.map { project ->
            val upcoming = upcomingByProjectId[project.id] ?: return@map project
            project.copy(
                name = project.name.ifBlank { upcoming.name },
                courseName = project.courseName ?: upcoming.courseName,
                groupName = project.groupName ?: upcoming.groupName,
                status = project.status ?: upcoming.status,
                deadline = listOfNotNull(project.deadline, upcoming.deadline).minOrNull(),
                steps = (project.steps + upcoming.steps).distinctBy { it.id },
                year = project.year ?: upcoming.year,
                courseId = project.courseId ?: upcoming.courseId,
                groups = (project.groups + upcoming.groups).distinctBy { it.id }
            )
        }
        val existingProjectIds = projects.map { it.id }.toSet()
        return copy(projects = mergedProjects + nextProjectStepProjects.filter { it.id !in existingProjectIds })
    }
}

internal data class AgendaWindow(
    val start: Long,
    val end: Long
) {
    companion object {
        fun firstSync(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): AgendaWindow {
            val start = LocalDate.of(2023, 9, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val end = today.plusDays(365)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
            return AgendaWindow(start, end)
        }

        fun subsequentSync(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): AgendaWindow {
            val start = today.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val end = today.plusDays(365)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
            return AgendaWindow(start, end)
        }

        fun fromToday(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): AgendaWindow {
            val start = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val end = today.plusDays(27)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
            return AgendaWindow(start, end)
        }
    }
}

internal fun academicYearCandidates(
    apiYears: List<String>,
    profileYear: String?,
    currentYear: Int = Year.now().value
): List<String> {
    val declaredYears = (apiYears + listOfNotNull(profileYear, currentYear.toString()))
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val startYear = declaredYears.mapNotNull { it.toAcademicYearInt() }.maxOrNull() ?: currentYear
    val descendingYears = (startYear downTo startYear - YEAR_FALLBACK_DEPTH).map { it.toString() }
    return (descendingYears + declaredYears.sortedByDescending { it.toAcademicYearInt() ?: Int.MIN_VALUE }).distinct()
}

private fun String.toAcademicYearInt(): Int? {
    return Regex("\\d{4}").find(this)?.value?.toIntOrNull()
}

private fun Grade.academicYearStart(): String? {
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(period.orEmpty())?.value?.take(4)?.let { return it }
    return date?.let { if (it.monthValue >= 9) it.year else it.year - 1 }?.toString()
}

private fun Absence.academicYearStart(): String? {
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(period.orEmpty())?.value?.take(4)?.let { return it }
    val date = startsAt.atZone(ZoneOffset.UTC).toLocalDate()
    return (if (date.monthValue >= 9) date.year else date.year - 1).toString()
}

private const val YEAR_FALLBACK_DEPTH = 10

private data class DashboardLocalData(
    val profile: com.elg.studly.adapters.secondary.storage.StudentProfileEntity?,
    val agenda: List<AgendaEvent>,
    val grades: List<Grade>,
    val absences: List<Absence>,
    val projects: List<Project>
)
