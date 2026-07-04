package com.elg.studly.application.ports

import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.CalendarAccount
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectMessage
import com.elg.studly.domain.model.ReminderTarget
import com.elg.studly.domain.model.Session
import com.elg.studly.domain.model.StudentEvent
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.AgendaColorMode
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

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
    fun observeEvents(): Flow<List<StudentEvent>>
    suspend fun syncAll(force: Boolean = false, features: Set<SyncFeature>? = null)
    suspend fun clearCache()
    suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit = {}): String
    suspend fun joinGroup(courseId: String, projectId: String, groupId: String)
    suspend fun leaveGroup(courseId: String, projectId: String, groupId: String)
    suspend fun subscribeEvent(eventId: String)
    suspend fun unsubscribeEvent(eventId: String)
    suspend fun projectMessages(groupId: String): List<ProjectMessage> = emptyList()
    suspend fun sendProjectMessage(groupId: String, message: String) = Unit
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
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setAgendaColorMode(mode: AgendaColorMode)
    suspend fun setRefreshInterval(feature: SyncFeature, minutes: Int)
    suspend fun setClassReminderLeadMinutes(minutes: Int)
    suspend fun setDeadlineReminderLeadMinutes(minutes: Int)
    suspend fun lastFetchedAt(feature: SyncFeature): Instant?
    suspend fun markFeatureFetched(feature: SyncFeature)
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
    suspend fun scheduleStudentSync(intervalMinutes: Long)
    suspend fun runStudentSyncNow()
    suspend fun cancelStudentSync()
    suspend fun showSyncFailure()
    suspend fun showNewGrade(grade: Grade)
    suspend fun showNewAbsence(absence: Absence)
    suspend fun showAgendaChange(event: AgendaEvent)
    suspend fun showProjectDeadline(project: Project)
    suspend fun showNewDocument(document: AcademicDocument)

    
    suspend fun scheduleReminders(targets: List<ReminderTarget>, classLeadMinutes: Int, deadlineLeadMinutes: Int)
}
