package com.elg.myges.adapters.secondary.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.elg.myges.adapters.secondary.api.MyGesApiService
import com.elg.myges.adapters.secondary.api.toAbsences
import com.elg.myges.adapters.secondary.api.toAgendaEvents
import com.elg.myges.adapters.secondary.api.toCourseSyllabus
import com.elg.myges.adapters.secondary.api.toCourses
import com.elg.myges.adapters.secondary.api.toDocuments
import com.elg.myges.adapters.secondary.api.toGrades
import com.elg.myges.adapters.secondary.api.toNews
import com.elg.myges.adapters.secondary.api.toNextProjectStepProjects
import com.elg.myges.adapters.secondary.api.toPracticals
import com.elg.myges.adapters.secondary.api.toProfile
import com.elg.myges.adapters.secondary.api.toProjectDocuments
import com.elg.myges.adapters.secondary.api.toProjects
import com.elg.myges.adapters.secondary.api.toYears
import com.elg.myges.adapters.secondary.storage.StudentDao
import com.elg.myges.adapters.secondary.storage.toDomain
import com.elg.myges.adapters.secondary.storage.toEntity
import com.elg.myges.adapters.secondary.storage.toStepEntities
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

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
                latestGrades = localData.grades.sortedWith(compareByDescending<Grade> { it.date }.thenByDescending { it.id }).take(3),
                recentAbsences = localData.absences.sortedByDescending { it.startsAt }.take(1),
                dueProjects = localData.projects.filter { it.deadline?.isAfter(now) == true }.take(3),
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
        return combine(dao.observeProjects(), dao.observeProjectSteps()) { projects, steps ->
            val stepsByProject = steps.groupBy { it.projectId }
            projects.map { project -> project.toDomain(stepsByProject[project.id].orEmpty()) }
        }
    }

    override fun observePracticals(): Flow<List<Practical>> {
        return dao.observePracticals().map { practicals -> practicals.map { it.toDomain() } }
    }

    override fun observeDocuments(): Flow<List<AcademicDocument>> {
        return dao.observeDocuments().map { documents -> documents.map { it.toDomain() } }
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

                val isFirstSync = dao.agendaIds().isEmpty()
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
                val yearData = fetchAllYearsData(activeYears)
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
                    projectSteps = yearData.projects.flatMap { it.toStepEntities() },
                    practicals = yearData.practicals.map { it.toEntity() },
                    documents = yearData.documents.map { it.toEntity() },
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
                val remoteUrl = document.downloadUrl ?: "me/annualDocuments/${document.id}"
                val response = api.download(remoteUrl)
                val directory = File(context.cacheDir, DOCUMENT_CACHE).apply { mkdirs() }
                purgeExpiredDocumentCache(directory)
                val target = File(directory, document.fileName.sanitizedFileName())
                val contentLength = response.contentLength()
                var copied = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                onProgress(0f.takeIf { contentLength > 0 })
                response.byteStream().use { input ->
                    target.outputStream().use { output ->
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
                AppException(AppError.Remote(code(), message()))
            }
            is IOException -> AppException(AppError.Network)
            else -> AppException(AppError.Unexpected(message))
        }
    }

    private fun String.sanitizedFileName(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "document" }
    }

    private suspend fun fetchAllYearsData(years: List<String>): YearData {
        val allCourses = mutableListOf<Course>()
        val allGrades = mutableListOf<Grade>()
        val allAbsences = mutableListOf<Absence>()
        val allDocuments = mutableListOf<AcademicDocument>()
        val allProjects = mutableListOf<Project>()
        val allPracticals = mutableListOf<Practical>()
        val fetchedCourseDocIds = mutableSetOf<String>()

        years.forEach { year ->
            val courses = runCatching { api.courses(year)?.toCourses().orEmpty() }.getOrDefault(emptyList())
                .withRemoteSyllabus()
            val grades = runCatching { api.grades(year)?.toGrades(year).orEmpty() }.getOrDefault(emptyList())
            val projectsJson = runCatching { api.projects(year) }.getOrNull()
            val projects = projectsJson?.toProjects().orEmpty()
            
            val newCourses = courses.filter { it.id !in fetchedCourseDocIds }
            fetchedCourseDocIds.addAll(newCourses.map { it.id })
            
            val documents = runCatching { api.annualDocuments(year)?.toDocuments().orEmpty() }.getOrDefault(emptyList()) +
                courseDocuments(newCourses) +
                projectsJson?.toProjectDocuments().orEmpty()
                
            val availablePeriods = (grades.mapNotNull { it.period } + courses.mapNotNull { it.period })
                .filter { it.isNotBlank() }
                .distinct()
            val absences = runCatching { api.absences(year)?.toAbsences(year, availablePeriods).orEmpty() }.getOrDefault(emptyList())
            val practicals = runCatching { api.practicals(year)?.toPracticals().orEmpty() }.getOrDefault(emptyList())

            allCourses.addAll(courses)
            allGrades.addAll(grades)
            allAbsences.addAll(absences)
            allDocuments.addAll(documents)
            allProjects.addAll(projects)
            allPracticals.addAll(practicals)
        }

        return YearData(
            courses = allCourses.distinctBy { it.id },
            grades = allGrades.distinctBy { it.id },
            absences = allAbsences.distinctBy { it.id },
            documents = allDocuments.distinctBy { it.id },
            projects = allProjects.distinctBy { it.id },
            practicals = allPracticals.distinctBy { it.id }
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
                            document.copy(downloadUrl = document.downloadUrl ?: "me/${course.id}/files/${document.id}")
                        }
                }.getOrDefault(emptyList())
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
    val practicals: List<Practical>
) {
    fun hasAcademicData(): Boolean {
        return courses.isNotEmpty() ||
            grades.isNotEmpty() ||
            absences.isNotEmpty() ||
            documents.isNotEmpty() ||
            projects.isNotEmpty() ||
            practicals.isNotEmpty()
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
                steps = (project.steps + upcoming.steps).distinctBy { it.id }
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

private const val YEAR_FALLBACK_DEPTH = 10

private data class DashboardLocalData(
    val profile: com.elg.myges.adapters.secondary.storage.StudentProfileEntity?,
    val agenda: List<AgendaEvent>,
    val grades: List<Grade>,
    val absences: List<Absence>,
    val projects: List<Project>
)
