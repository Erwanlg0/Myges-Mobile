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
import com.elg.studly.adapters.secondary.storage.ProjectGroupEntity
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
import com.elg.studly.domain.model.ProjectGroup
import com.elg.studly.domain.model.StudentProfile
import com.elg.studly.domain.model.SyncFeature
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.time.Duration
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

    override suspend fun syncAll(force: Boolean, features: Set<SyncFeature>?) {
        withContext(Dispatchers.IO) {
            try {
                purgeExpiredDocumentCache(File(context.cacheDir, DOCUMENT_CACHE))

                val due = features ?: dueFeatures(force)
                if (due.isEmpty()) return@withContext

                val needAgenda = SyncFeature.Agenda in due
                val needGrades = SyncFeature.Grades in due
                val needAbsences = SyncFeature.Absences in due
                val needProjects = SyncFeature.Projects in due
                val needDocuments = SyncFeature.Documents in due
                val needDirectory = SyncFeature.Directory in due
                val needNews = SyncFeature.News in due
                
                val academicDue = needProjects || needDocuments
                val needYears = needGrades || needAbsences || needDirectory || academicDue

                val profileAndYears = if (needYears) {
                    val cachedProfile = dao.profile()
                    if (!force && cachedProfile != null && !cachedProfile.academicYear.isNullOrBlank()) {
                        val activeYears = cachedProfile.academicYear.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        ProfileAndYears(cachedProfile.toDomain(), activeYears)
                    } else {
                        fetchProfileAndYears()
                    }
                } else null
                val updatedProfile = profileAndYears?.profile
                val years = profileAndYears?.years.orEmpty()

                val today = LocalDate.now(ZoneOffset.UTC)
                val currentAcademicYearStart = (if (today.monthValue >= 9) today.year else today.year - 1).toString()
                
                val isFirstSync = settingsRepository.settings.first().lastSyncAt == null
                val finalSyncYears = if (force || isFirstSync) years else years.filter { it >= currentAcademicYearStart }.ifEmpty { years }

                val nextProjectStepProjects = if (academicDue) {
                    runCatching { api.nextProjectSteps()?.toNextProjectStepProjects().orEmpty() }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                val fetched = coroutineScope {
                    val agendaDeferred = if (needAgenda) async { fetchAgendaEvents() } else null
                    val gradesDeferred = if (needGrades) async { fetchGrades(years) } else null
                    val academicDeferred = if (academicDue) {
                        async { fetchAcademicData(finalSyncYears, updatedProfile!!.id, nextProjectStepProjects) }
                    } else {
                        null
                    }
                    val directoryDeferred = if (needDirectory) async { fetchDirectory(finalSyncYears) } else null
                    val newsDeferred = if (needNews) async { fetchNews() } else null

                    val agenda = agendaDeferred?.await()
                    val grades = gradesDeferred?.await()
                    val academic = academicDeferred?.await()
                    val directory = directoryDeferred?.await()
                    val news = newsDeferred?.await()
                    val absences = if (needAbsences) {
                        fetchAbsences(years, absencePeriods(grades, academic?.courses))
                    } else {
                        null
                    }

                    FetchedData(agenda, grades, absences, academic, directory, news)
                }

                val previousIds = SyncedIds(
                    agenda = if (needAgenda) dao.agendaIds().toSet() else emptySet(),
                    grades = if (needGrades) dao.gradeIds().toSet() else emptySet(),
                    absences = if (needAbsences) dao.absenceIds().toSet() else emptySet(),
                    projects = if (needProjects) dao.projectIds().toSet() else emptySet(),
                    documents = if (needDocuments) dao.documentIds().toSet() else emptySet()
                )

                updatedProfile?.let { dao.syncProfile(it.toEntity()) }
                fetched.agenda?.let { events -> dao.syncAgenda(events.map { it.toEntity() }) }
                fetched.grades?.let { grades -> dao.syncGrades(grades.map { it.toEntity() }) }
                fetched.absences?.let { absences -> dao.syncAbsences(absences.map { it.toEntity() }) }
                fetched.academic?.let { academic ->
                    dao.syncCourses(academic.courses.map { it.toEntity() })
                    if (needProjects) {
                        dao.syncProjectsAndPracticals(
                            projects = academic.projects.map { it.toEntity() },
                            projectGroups = academic.projects.flatMap { it.toGroupEntities() } +
                                academic.practicals.flatMap { it.toGroupEntities() },
                            projectSteps = academic.projects.flatMap { it.toStepEntities() } +
                                academic.practicals.flatMap { it.toStepEntities() },
                            practicals = academic.practicals.map { it.toEntity() }
                        )
                    }
                    if (needDocuments) {
                        dao.syncDocuments(academic.documents.map { it.toEntity() })
                    }
                }
                fetched.directory?.let { people -> dao.syncDirectory(people.map { it.toEntity() }) }
                fetched.news?.let { news -> dao.syncNews(news.distinctBy { it.id }.map { it.toEntity() }) }

                notifyAboutChanges(
                    previousIds = previousIds,
                    agenda = fetched.agenda.orEmpty(),
                    grades = fetched.grades.orEmpty(),
                    absences = fetched.absences.orEmpty(),
                    projects = if (needProjects) fetched.academic?.projects.orEmpty() else emptyList(),
                    documents = if (needDocuments) fetched.academic?.documents.orEmpty() else emptyList()
                )

                due.forEach { settingsRepository.markFeatureFetched(it) }
            } catch (throwable: Throwable) {
                throw throwable.toRepositoryException()
            }
        }
    }

    private suspend fun dueFeatures(force: Boolean): Set<SyncFeature> {
        if (force) return SyncFeature.entries.toSet()
        val intervals = settingsRepository.settings.first().refreshIntervals
        val now = Instant.now()
        return SyncFeature.entries.filterTo(mutableSetOf()) { feature ->
            val last = settingsRepository.lastFetchedAt(feature)
            last == null || Duration.between(last, now).toMinutes() >= intervals.minutesFor(feature)
        }
    }

    private suspend fun fetchProfileAndYears(): ProfileAndYears {
        val profile = api.profile().toProfile()
        val activeYears = (
            api.years()?.toYears().orEmpty() +
                runCatching { api.trimesterYears()?.toYears().orEmpty() }.getOrDefault(emptyList()) +
                listOfNotNull(profile.academicYear)
            )
            .flatMap { yearStr -> Regex("\\d{4}").findAll(yearStr).map { it.value } }
            .distinct()
            .sortedDescending()
            .ifEmpty { listOf(Year.now().value.toString()) }
        val updatedProfile = profile.copy(academicYear = activeYears.sorted().joinToString(", "))
        return ProfileAndYears(updatedProfile, activeYears)
    }

    private suspend fun fetchAgendaEvents(): List<AgendaEvent> {
        val isFirstSync = settingsRepository.settings.first().lastSyncAt == null || dao.agendaIds().isEmpty()
        val window = if (isFirstSync) AgendaWindow.firstSync() else AgendaWindow.subsequentSync()
        return api.agenda(start = window.start, end = window.end)?.toAgendaEvents().orEmpty()
    }

    private suspend fun fetchGrades(years: List<String>): List<Grade> {
        return years.flatMap { year ->
            runCatching { api.grades(year)?.toGrades(year).orEmpty() }.getOrDefault(emptyList())
        }.distinctBy { it.id }
    }

    private suspend fun fetchAbsences(years: List<String>, periods: List<String>): List<Absence> {
        return years.flatMap { year ->
            runCatching { api.absences(year)?.toAbsences(year, periods).orEmpty() }.getOrDefault(emptyList())
        }.distinctBy { it.id }
    }

    private suspend fun fetchDirectory(years: List<String>): List<DirectoryPerson> {
        return years.flatMap { directoryPeople(it) }.distinctBy { it.id }
    }

    private suspend fun fetchNews(): List<NewsItem> {
        return runCatching { api.minimumVersion()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
            api.news()?.toNews().orEmpty() +
            runCatching { api.newsBanners()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
            runCatching { api.partners()?.toNews().orEmpty() }.getOrDefault(emptyList()) +
            runCatching { api.speedMeetingAppointments()?.toNews().orEmpty() }.getOrDefault(emptyList())
    }

    
    private suspend fun absencePeriods(grades: List<Grade>?, courses: List<Course>?): List<String> {
        val resolvedGrades = grades ?: dao.grades().map { it.toDomain() }
        val resolvedCourses = courses ?: dao.courses().map { it.toDomain() }
        return (resolvedGrades.mapNotNull { it.period } + resolvedCourses.mapNotNull { it.period })
            .filter { it.isNotBlank() && it.contains(Regex("\\d{4}")) }
            .distinct()
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
                    val remoteUrl = document.normalizedDownloadUrl()
                    val response = api.download(remoteUrl)
                    if (!response.isSuccessful) throw HttpException(response)
                    val body = response.body() ?: throw AppException(AppError.EmptyResponse)
                    
                    
                    
                    val finalUrl = response.raw().request.url
                    val isLoginRedirect = finalUrl.host.contains("cas", ignoreCase = true) ||
                        finalUrl.encodedPath.contains("login", ignoreCase = true) ||
                        finalUrl.encodedPath.contains("j_spring", ignoreCase = true)
                    val isHtml = body.contentType()?.let {
                        it.type == "text" && it.subtype.equals("html", ignoreCase = true)
                    } == true
                    if (isLoginRedirect || isHtml) throw AppException(AppError.DocumentUnavailable)
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

    override suspend fun joinGroup(courseId: String, projectId: String, groupId: String) {
        changeGroupMembership(courseId, projectId) { api.joinGroup(courseId, projectId, groupId) }
    }

    override suspend fun leaveGroup(courseId: String, projectId: String, groupId: String) {
        changeGroupMembership(courseId, projectId) { api.leaveGroup(courseId, projectId, groupId) }
    }

    private suspend fun changeGroupMembership(
        courseId: String,
        projectId: String,
        action: suspend () -> Response<ResponseBody>
    ) {
        withContext(Dispatchers.IO) {
            val failure = try {
                val response = action()
                if (!response.isSuccessful) HttpException(response) else null
            } catch (throwable: Throwable) {
                throwable
            }
            refreshProjectGroups(courseId, projectId)
            failure?.let { throw it.toRepositoryException() }
        }
    }

    private suspend fun refreshProjectGroups(courseId: String, projectId: String) {
        runCatching {
            delay(2_000)
            val currentUserId = dao.profile()?.id
            val year = dao.projects().firstOrNull { it.id == projectId }?.year
            val fromYear = year?.let { api.projects(it) }
                ?.toProjects(currentUserId)?.firstOrNull { it.id == projectId }?.groups.orEmpty()
            val fromCourse = api.courseProjects(courseId)
                ?.toProjects(currentUserId)?.firstOrNull { it.id == projectId }?.groups.orEmpty()
            val groups = mergeGroups(fromYear, fromCourse)
            if (groups.isNotEmpty()) {
                dao.replaceGroupsForProject(projectId, groups.map { group ->
                    ProjectGroupEntity(
                        projectId = projectId,
                        id = group.id,
                        name = group.name,
                        students = group.students.joinToString("\n"),
                        isMine = group.isMine
                    )
                })
            }
        }
    }

    private fun AcademicDocument.normalizedDownloadUrl(): String {
        val url = downloadUrl ?: return "me/annualDocuments/$id"
        
        
        
        val parsed = url.toHttpUrlOrNull()
        if (parsed != null && parsed.host.contains("ges-dl", ignoreCase = true)) {
            val courseId = parsed.queryParameter("courseId")
            if (!courseId.isNullOrBlank()) return "me/$courseId/files/$id"
        }
        return url
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
            "text/csv" -> "csv"
            "text/html" -> "html"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/gif" -> "gif"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.ms-excel" -> "xls"
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            "application/vnd.ms-powerpoint" -> "ppt"
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"
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

    private suspend fun fetchAcademicData(
        years: List<String>,
        currentUserId: String,
        nextProjectStepProjects: List<Project>
    ): AcademicData {
        val allCourses = mutableListOf<Course>()
        val allDocuments = mutableListOf<AcademicDocument>()
        val allProjects = mutableListOf<Project>()
        val allPracticals = mutableListOf<Practical>()
        val fetchedCourseDocIds = mutableSetOf<String>()

        years.forEach { year ->
            val courses = runCatching { api.courses(year)?.toCourses().orEmpty() }.getOrDefault(emptyList())
                .withRemoteSyllabus()
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

            val myProjectIds = projects.filter { it.groups.isEmpty() || it.groups.any { g -> g.isMine } }.map { it.id }.toSet()
            val myPracticalIds = practicals.filter { it.groups.isEmpty() || it.groups.any { g -> g.isMine } }.map { it.id }.toSet()

            val documents = runCatching { api.annualDocuments(year)?.toDocuments(year).orEmpty() }.getOrDefault(emptyList()) +
                courseDocuments(newCourses) +
                projectPayloads.flatMap { it.toProjectDocuments(year) }.filter { it.ownerId in myProjectIds } +
                practicalPayloads.flatMap { it.toPracticalDocuments(year) }.filter { it.ownerId in myPracticalIds } +
                syllabusDocuments(courses)

            allCourses.addAll(courses)
            allDocuments.addAll(documents)
            allProjects.addAll(projects)
            allPracticals.addAll(practicals)
        }

        return AcademicData(
            courses = allCourses.distinctBy { it.id },
            projects = allProjects.mergeProjects().withNextProjectSteps(nextProjectStepProjects),
            practicals = allPracticals.distinctBy { it.id },
            documents = allDocuments.distinctBy { it.id }
        )
    }

    private fun List<Project>.withNextProjectSteps(nextProjectStepProjects: List<Project>): List<Project> {
        if (nextProjectStepProjects.isEmpty()) return this
        val upcomingByProjectId = nextProjectStepProjects.associateBy { it.id }
        val mergedProjects = map { project ->
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
        val existingProjectIds = map { it.id }.toSet()
        return mergedProjects + nextProjectStepProjects.filter { it.id !in existingProjectIds }
    }

    private suspend fun courseDocuments(courses: List<Course>): List<AcademicDocument> {
        return courses.filter { it.fileCount > 0 }
            .flatMap { course ->
                runCatching {
                    api.courseFiles(course.id)
                        ?.toDocuments()
                        .orEmpty()
                        .map { document ->
                            
                            
                            
                            
                            document.copy(downloadUrl = "me/${course.id}/files/${document.id}")
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
                    groups = mergeGroups(current.groups, next.groups),
                    groupMode = current.groupMode ?: next.groupMode
                )
            }
        }
    }

    
    private fun mergeGroups(a: List<ProjectGroup>, b: List<ProjectGroup>): List<ProjectGroup> {
        return (a + b).groupBy { it.id }.values.map { duplicates ->
            duplicates.reduce { current, next ->
                current.copy(
                    name = current.name.ifBlank { next.name },
                    students = if (current.students.size >= next.students.size) current.students else next.students,
                    isMine = current.isMine || next.isMine
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
        val today = LocalDate.now(ZoneOffset.UTC)
        val currentAcademicYearStart = (if (today.monthValue >= 9) today.year else today.year - 1).toString()

        if (settings.notifications.agenda && previousIds.agenda.isNotEmpty()) {
            agenda.filter { it.id !in previousIds.agenda }.forEach { notificationScheduler.showAgendaChange(it) }
        }
        if (settings.notifications.grades && previousIds.grades.isNotEmpty()) {
            grades.filter { it.id !in previousIds.grades && (it.academicYearStart() == null || it.academicYearStart()!! >= currentAcademicYearStart) }
                .forEach { notificationScheduler.showNewGrade(it) }
        }
        if (settings.notifications.absences && previousIds.absences.isNotEmpty()) {
            absences.filter { it.id !in previousIds.absences && (it.academicYearStart() == null || it.academicYearStart()!! >= currentAcademicYearStart) }
                .forEach { notificationScheduler.showNewAbsence(it) }
        }
        if (settings.notifications.projects && previousIds.projects.isNotEmpty()) {
            projects.filter { it.id !in previousIds.projects && (it.year == null || it.year!! >= currentAcademicYearStart) }
                .forEach { notificationScheduler.showProjectDeadline(it) }
        }
        if (settings.notifications.documents && previousIds.documents.isNotEmpty()) {
            documents.filter { it.id !in previousIds.documents && (it.year == null || it.year!! >= currentAcademicYearStart) }
                .forEach { notificationScheduler.showNewDocument(it) }
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

private data class ProfileAndYears(
    val profile: StudentProfile,
    val years: List<String>
)

private data class AcademicData(
    val courses: List<Course>,
    val projects: List<Project>,
    val practicals: List<Practical>,
    val documents: List<AcademicDocument>
)

private data class FetchedData(
    val agenda: List<AgendaEvent>?,
    val grades: List<Grade>?,
    val absences: List<Absence>?,
    val academic: AcademicData?,
    val directory: List<DirectoryPerson>?,
    val news: List<NewsItem>?
)

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
