package com.elg.myges.application.usecase

import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.Session
import com.elg.myges.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class CompleteOAuthLoginUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        sessionRepository.authenticateWithToken(accessToken, expiresAt, enableBiometric)
        notificationScheduler.ensureChannels()
        notificationScheduler.scheduleStudentSync()
    }
}

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Session?> = sessionRepository.session
}

class ObserveLockedBiometricSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Boolean> = sessionRepository.hasLockedBiometricSession
}

class UnlockWithBiometricsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() = sessionRepository.unlockWithBiometrics()
}

class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke() {
        notificationScheduler.cancelStudentSync()
        sessionRepository.logout()
    }
}

class ObserveDashboardUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<DashboardSummary> = repository.observeDashboard()
}

class ObserveAgendaUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<AgendaEvent>> = repository.observeAgenda()
}

class ObserveGradesUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<Grade>> = repository.observeGrades()
}

class ObserveAbsencesUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<Absence>> = repository.observeAbsences()
}

class ObserveCoursesUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<Course>> = repository.observeCourses()
}

class ObserveProjectsUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<Project>> = repository.observeProjects()
}

class ObservePracticalsUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<Practical>> = repository.observePracticals()
}

class ObserveDocumentsUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<AcademicDocument>> = repository.observeDocuments()
}

class ObserveNewsUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<NewsItem>> = repository.observeNews()
}

class RefreshStudentDataUseCase @Inject constructor(
    private val repository: StudentDataRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarSyncPort: CalendarSyncPort
) {
    suspend operator fun invoke() {
        repository.syncAll()
        syncCalendarIfEnabled()
        settingsRepository.markSynced()
    }

    private suspend fun syncCalendarIfEnabled() {
        val settings = settingsRepository.settings.first()
        if (settings.calendarSyncEnabled) {
            runCatching { calendarSyncPort.sync(repository.observeAgenda().first()) }
        }
    }
}

class ClearCacheUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    suspend operator fun invoke() = repository.clearCache()
}

class DownloadDocumentUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    suspend operator fun invoke(document: AcademicDocument) = repository.downloadDocument(document)
}

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> = repository.settings
}

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun language(languageTag: String?) = repository.setLanguageTag(languageTag)
    suspend fun calendarSync(enabled: Boolean) = repository.setCalendarSyncEnabled(enabled)
    suspend fun gradeNotifications(enabled: Boolean) = repository.setGradeNotificationsEnabled(enabled)
    suspend fun absenceNotifications(enabled: Boolean) = repository.setAbsenceNotificationsEnabled(enabled)
    suspend fun agendaNotifications(enabled: Boolean) = repository.setAgendaNotificationsEnabled(enabled)
    suspend fun projectNotifications(enabled: Boolean) = repository.setProjectNotificationsEnabled(enabled)
    suspend fun documentNotifications(enabled: Boolean) = repository.setDocumentNotificationsEnabled(enabled)
}

class SyncAgendaToCalendarUseCase @Inject constructor(
    private val calendarSyncPort: CalendarSyncPort
) {
    suspend operator fun invoke(events: List<AgendaEvent>) {
        calendarSyncPort.sync(events)
    }
}
