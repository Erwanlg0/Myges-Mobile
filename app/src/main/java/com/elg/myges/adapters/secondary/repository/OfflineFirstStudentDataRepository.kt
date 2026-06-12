package com.elg.myges.adapters.secondary.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.elg.myges.adapters.secondary.api.MyGesApiService
import com.elg.myges.adapters.secondary.api.toAbsences
import com.elg.myges.adapters.secondary.api.toAgendaEvents
import com.elg.myges.adapters.secondary.api.toCourses
import com.elg.myges.adapters.secondary.api.toDocuments
import com.elg.myges.adapters.secondary.api.toGrades
import com.elg.myges.adapters.secondary.api.toNews
import com.elg.myges.adapters.secondary.api.toPracticals
import com.elg.myges.adapters.secondary.api.toProfile
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
                latestGrades = localData.grades.sortedByDescending { it.date }.take(3),
                recentAbsences = localData.absences.take(3),
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
                val profile = api.profile().toProfile()
                val year = api.years().toYears().firstOrNull()
                    ?: profile.academicYear
                    ?: Year.now().value.toString()
                val agendaWindow = AgendaWindow.fromToday()
                val agenda = api.agenda(
                    start = agendaWindow.start,
                    end = agendaWindow.end
                ).toAgendaEvents()
                val courses = api.courses(year).toCourses()
                val grades = api.grades(year).toGrades()
                val absences = api.absences(year).toAbsences()
                val documents = api.annualDocuments(year).toDocuments()
                val projects = api.projects(year).toProjects()
                val practicals = api.practicals(year).toPracticals()
                val news = api.news().toNews() + runCatching { api.newsBanners().toNews() }.getOrDefault(emptyList())
                val previousIds = SyncedIds(
                    agenda = dao.agendaIds().toSet(),
                    grades = dao.gradeIds().toSet(),
                    absences = dao.absenceIds().toSet(),
                    projects = dao.projectIds().toSet(),
                    documents = dao.documentIds().toSet()
                )
                dao.replaceSyncedData(
                    profile = profile.toEntity(),
                    agenda = agenda.map { it.toEntity() },
                    grades = grades.map { it.toEntity() },
                    absences = absences.map { it.toEntity() },
                    courses = courses.map { it.toEntity() },
                    projects = projects.map { it.toEntity() },
                    projectSteps = projects.flatMap { it.toStepEntities() },
                    practicals = practicals.map { it.toEntity() },
                    documents = documents.map { it.toEntity() },
                    news = news.distinctBy { it.id }.map { it.toEntity() }
                )
                notifyAboutChanges(previousIds, agenda, grades, absences, projects, documents)
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

    override suspend fun downloadDocument(document: AcademicDocument): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val remoteUrl = document.downloadUrl ?: "me/annualDocuments/${document.id}"
                val response = api.download(remoteUrl)
                val directory = File(context.cacheDir, DOCUMENT_CACHE).apply { mkdirs() }
                val target = File(directory, document.fileName.sanitizedFileName())
                response.byteStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
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

private data class AgendaWindow(
    val start: Long,
    val end: Long
) {
    companion object {
        fun fromToday(): AgendaWindow {
            val today = LocalDate.now(ZoneOffset.UTC)
            val start = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val end = today.plusDays(180)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
            return AgendaWindow(start, end)
        }
    }
}

private data class DashboardLocalData(
    val profile: com.elg.myges.adapters.secondary.storage.StudentProfileEntity?,
    val agenda: List<AgendaEvent>,
    val grades: List<Grade>,
    val absences: List<Absence>,
    val projects: List<Project>
)
