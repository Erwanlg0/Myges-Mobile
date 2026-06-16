package com.elg.myges.application.ports

import android.net.Uri
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.CalendarAccount
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.Session
import com.elg.myges.domain.model.ThemeMode
import com.elg.myges.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SessionRepository {
    val session: Flow<Session?>
    val hasLockedBiometricSession: Flow<Boolean>
    fun currentSession(): Session?
    fun invalidateSession()
    suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean)
    suspend fun unlockWithBiometrics()
    suspend fun logout()
}

interface StudentDataRepository {
    fun observeDashboard(): Flow<DashboardSummary>
    fun observeAgenda(): Flow<List<AgendaEvent>>
    fun observeGrades(): Flow<List<Grade>>
    fun observeAbsences(): Flow<List<Absence>>
    fun observeCourses(): Flow<List<Course>>
    fun observeProjects(): Flow<List<Project>>
    fun observePracticals(): Flow<List<Practical>>
    fun observeDocuments(): Flow<List<AcademicDocument>>
    fun observeDirectory(): Flow<List<DirectoryPerson>>
    fun observeNews(): Flow<List<NewsItem>>
    suspend fun syncAll()
    suspend fun clearCache()
    suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit = {}): Uri
}

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setLanguageTag(languageTag: String?)
    suspend fun setCalendarSyncEnabled(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setGradeNotificationsEnabled(enabled: Boolean)
    suspend fun setAbsenceNotificationsEnabled(enabled: Boolean)
    suspend fun setAgendaNotificationsEnabled(enabled: Boolean)
    suspend fun setProjectNotificationsEnabled(enabled: Boolean)
    suspend fun setDocumentNotificationsEnabled(enabled: Boolean)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun markSynced()
    suspend fun clearSyncMetadata()
}

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

interface CalendarSyncPort {
    suspend fun sync(events: List<AgendaEvent>)
    suspend fun availableCalendars(): List<CalendarAccount>
    suspend fun selectedCalendarId(): Long?
    suspend fun selectCalendar(id: Long)
}

interface NotificationScheduler {
    fun ensureChannels()
    suspend fun scheduleStudentSync()
    suspend fun runStudentSyncNow()
    suspend fun cancelStudentSync()
    suspend fun showSyncFailure()
    suspend fun showNewGrade(grade: Grade)
    suspend fun showNewAbsence(absence: Absence)
    suspend fun showAgendaChange(event: AgendaEvent)
    suspend fun showProjectDeadline(project: Project)
    suspend fun showNewDocument(document: AcademicDocument)
}
