package com.elg.studly.application.usecase

import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.Session
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class CompleteOAuthLoginUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        sessionRepository.authenticateWithToken(accessToken, expiresAt, enableBiometric)
        notificationScheduler.ensureChannels()
        val intervalMinutes = settingsRepository.settings.first().refreshIntervals.smallestIntervalMinutes().toLong()
        notificationScheduler.scheduleStudentSync(intervalMinutes)
        notificationScheduler.runStudentSyncNow()
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

class ObserveDirectoryUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    operator fun invoke(): Flow<List<DirectoryPerson>> = repository.observeDirectory()
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
    /** [force] = true bypasses the per-feature refresh intervals (manual refresh). */
    suspend operator fun invoke(force: Boolean = false) {
        repository.syncAll(force)
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
    private val repository: StudentDataRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.clearCache()
        settingsRepository.clearSyncMetadata()
    }
}

class DownloadDocumentUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    suspend operator fun invoke(document: AcademicDocument, onProgress: (Float?) -> Unit = {}) = repository.downloadDocument(document, onProgress)
}

class JoinGroupUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    suspend operator fun invoke(courseId: String, projectId: String, groupId: String) =
        repository.joinGroup(courseId, projectId, groupId)
}

class LeaveGroupUseCase @Inject constructor(
    private val repository: StudentDataRepository
) {
    suspend operator fun invoke(courseId: String, projectId: String, groupId: String) =
        repository.leaveGroup(courseId, projectId, groupId)
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
    suspend fun biometric(enabled: Boolean) = repository.setBiometricEnabled(enabled)
    suspend fun gradeNotifications(enabled: Boolean) = repository.setGradeNotificationsEnabled(enabled)
    suspend fun absenceNotifications(enabled: Boolean) = repository.setAbsenceNotificationsEnabled(enabled)
    suspend fun agendaNotifications(enabled: Boolean) = repository.setAgendaNotificationsEnabled(enabled)
    suspend fun projectNotifications(enabled: Boolean) = repository.setProjectNotificationsEnabled(enabled)
    suspend fun documentNotifications(enabled: Boolean) = repository.setDocumentNotificationsEnabled(enabled)
    suspend fun themeMode(themeMode: ThemeMode) = repository.setThemeMode(themeMode)
    suspend fun refreshInterval(feature: SyncFeature, minutes: Int) = repository.setRefreshInterval(feature, minutes)
}

/** Re-aligns the background sync worker cadence with the smallest configured refresh interval. */
class RescheduleSyncUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke() {
        val intervalMinutes = settingsRepository.settings.first().refreshIntervals.smallestIntervalMinutes().toLong()
        notificationScheduler.scheduleStudentSync(intervalMinutes)
    }
}

class SyncAgendaToCalendarUseCase @Inject constructor(
    private val calendarSyncPort: CalendarSyncPort
) {
    suspend operator fun invoke(events: List<AgendaEvent>) {
        calendarSyncPort.sync(events)
    }
}

class CalendarAccountsUseCase @Inject constructor(
    private val calendarSyncPort: CalendarSyncPort
) {
    suspend fun available() = calendarSyncPort.availableCalendars()
    suspend fun selected() = calendarSyncPort.selectedCalendarId()
    suspend fun select(id: Long) = calendarSyncPort.selectCalendar(id)
}
