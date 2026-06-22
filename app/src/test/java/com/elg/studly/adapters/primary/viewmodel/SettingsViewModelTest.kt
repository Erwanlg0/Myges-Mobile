package com.elg.studly.adapters.primary.viewmodel

import android.net.Uri
import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.application.usecase.CalendarAccountsUseCase
import com.elg.studly.application.usecase.ClearCacheUseCase
import com.elg.studly.application.usecase.LogoutUseCase
import com.elg.studly.application.usecase.ObserveSettingsUseCase
import com.elg.studly.application.usecase.RescheduleSyncUseCase
import com.elg.studly.application.usecase.UpdateSettingsUseCase
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.CalendarAccount
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.NotificationPreferences
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ReminderTarget
import com.elg.studly.domain.model.Session
import com.elg.studly.domain.model.StudentEvent
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setLanguageUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)
        val stateCollection = collectState(viewModel)

        viewModel.setLanguage("en")
        advanceUntilIdle()

        assertEquals("en", settingsRepository.languageTag)
        assertFalse(viewModel.state.value.loading)
        assertEquals(null, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun setDocumentNotificationsUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setDocumentNotifications(false)
        advanceUntilIdle()

        assertEquals(false, settingsRepository.documentNotifications)
    }

    @Test
    fun setCalendarSyncUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setCalendarSync(true)
        advanceUntilIdle()

        assertEquals(true, settingsRepository.calendarSyncEnabled)
    }

    @Test
    fun setBiometricUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setBiometricEnabled(true)
        advanceUntilIdle()

        assertEquals(true, settingsRepository.biometricEnabled)
    }

    @Test
    fun setNotificationsUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setGradeNotifications(false)
        viewModel.setAbsenceNotifications(false)
        viewModel.setAgendaNotifications(false)
        viewModel.setProjectNotifications(false)
        advanceUntilIdle()

        assertEquals(false, settingsRepository.gradeNotifications)
        assertEquals(false, settingsRepository.absenceNotifications)
        assertEquals(false, settingsRepository.agendaNotifications)
        assertEquals(false, settingsRepository.projectNotifications)
    }

    @Test
    fun setThemeAndColorUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setThemeMode(com.elg.studly.domain.model.ThemeMode.Dark)
        viewModel.setDynamicColor(true)
        advanceUntilIdle()

        assertEquals(com.elg.studly.domain.model.ThemeMode.Dark, settingsRepository.themeMode)
        assertEquals(true, settingsRepository.dynamicColorEnabled)
    }

    @Test
    fun setRefreshIntervalsUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setRefreshInterval(SyncFeature.Grades, 30)
        advanceUntilIdle()

        assertEquals(30, settingsRepository.refreshIntervals[SyncFeature.Grades])
    }



    @Test
    fun clearCacheClearsRepositoryAndSyncMetadata() = runTest(dispatcher) {
        val events = mutableListOf<String>()
        val viewModel = settingsViewModel(
            settingsRepository = RecordingSettingsRepository(events),
            studentDataRepository = RecordingStudentDataRepository(events)
        )

        viewModel.clearCache()
        advanceUntilIdle()

        assertEquals(listOf("clearCache", "clearSyncMetadata"), events)
    }

    @Test
    fun logoutCancelsSyncBeforeClearingSession() = runTest(dispatcher) {
        val events = mutableListOf<String>()
        val viewModel = settingsViewModel(
            sessionRepository = RecordingSessionRepository(events),
            notificationScheduler = RecordingNotificationScheduler(events)
        )

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(listOf("cancelSync", "logout"), events)
    }

    @Test
    fun settingFailureIsExposedAsUiError() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository().apply {
            failure = AppException(AppError.Storage)
        }
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)
        val stateCollection = collectState(viewModel)

        viewModel.setCalendarSync(true)
        advanceUntilIdle()

        assertEquals(AppError.Storage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
        stateCollection.cancel()
    }

    private fun collectState(viewModel: SettingsViewModel): Job {
        return kotlinx.coroutines.CoroutineScope(dispatcher).launch {
            viewModel.state.collect()
        }
    }

    private fun settingsViewModel(
        settingsRepository: RecordingSettingsRepository = RecordingSettingsRepository(),
        studentDataRepository: RecordingStudentDataRepository = RecordingStudentDataRepository(mutableListOf()),
        sessionRepository: RecordingSessionRepository = RecordingSessionRepository(mutableListOf()),
        notificationScheduler: RecordingNotificationScheduler = RecordingNotificationScheduler(mutableListOf())
    ): SettingsViewModel {
        return SettingsViewModel(
            ObserveSettingsUseCase(settingsRepository),
            UpdateSettingsUseCase(settingsRepository, studentDataRepository, notificationScheduler),
            ClearCacheUseCase(studentDataRepository, settingsRepository),
            LogoutUseCase(sessionRepository, notificationScheduler),
            CalendarAccountsUseCase(StubCalendarSyncPort()),
            RescheduleSyncUseCase(settingsRepository, notificationScheduler)
        )
    }
}

private class StubCalendarSyncPort : CalendarSyncPort {
    override suspend fun sync(events: List<AgendaEvent>) = Unit
    override suspend fun availableCalendars(): List<CalendarAccount> = emptyList()
    override suspend fun selectedCalendarId(): Long? = null
    override suspend fun selectCalendar(id: Long) = Unit
}

private class RecordingSettingsRepository(
    private val events: MutableList<String> = mutableListOf()
) : SettingsRepository {
    var failure: Throwable? = null
    var languageTag: String? = null
    var documentNotifications: Boolean? = null
    override val settings = MutableStateFlow(
        UserSettings(
            languageTag = null,
            notifications = NotificationPreferences(true, true, true, true, true),
            calendarSyncEnabled = false,
            lastSyncAt = null
        )
    )

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.biometricEnabled = enabled
    }

    override suspend fun setLanguageTag(languageTag: String?) {
        failure?.let { throw it }
        this.languageTag = languageTag
    }

    override suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.calendarSyncEnabled = enabled
    }

    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.gradeNotifications = enabled
    }

    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.absenceNotifications = enabled
    }

    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.agendaNotifications = enabled
    }

    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        this.projectNotifications = enabled
    }

    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        documentNotifications = enabled
    }



    var themeMode: com.elg.studly.domain.model.ThemeMode? = null
    var dynamicColorEnabled: Boolean? = null
    var calendarSyncEnabled: Boolean? = null
    var biometricEnabled: Boolean? = null
    var gradeNotifications: Boolean? = null
    var absenceNotifications: Boolean? = null
    var agendaNotifications: Boolean? = null
    var projectNotifications: Boolean? = null
    val refreshIntervals = mutableMapOf<SyncFeature, Int>()

    override suspend fun setThemeMode(themeMode: com.elg.studly.domain.model.ThemeMode) {
        this.themeMode = themeMode
    }
    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        this.dynamicColorEnabled = enabled
    }
    override suspend fun setAgendaColorMode(mode: com.elg.studly.domain.model.AgendaColorMode) = Unit
    override suspend fun setRefreshInterval(feature: SyncFeature, minutes: Int) {
        refreshIntervals[feature] = minutes
    }
    override suspend fun setClassReminderLeadMinutes(minutes: Int) = Unit
    override suspend fun setDeadlineReminderLeadMinutes(minutes: Int) = Unit
    override suspend fun lastFetchedAt(feature: SyncFeature): Instant? = null
    override suspend fun markFeatureFetched(feature: SyncFeature) = Unit

    override suspend fun markSynced() = Unit

    override suspend fun clearSyncMetadata() {
        events += "clearSyncMetadata"
    }
}

private class RecordingStudentDataRepository(
    private val events: MutableList<String>
) : StudentDataRepository {
    override fun observeDashboard(): Flow<DashboardSummary> = flowOf(DashboardSummary(null, null, emptyList(), emptyList(), emptyList(), null))
    override fun observeAgenda(): Flow<List<AgendaEvent>> = flowOf(emptyList())
    override fun observeGrades(): Flow<List<Grade>> = flowOf(emptyList())
    override fun observeAbsences(): Flow<List<Absence>> = flowOf(emptyList())
    override fun observeCourses(): Flow<List<Course>> = flowOf(emptyList())
    override fun observeProjects(): Flow<List<Project>> = flowOf(emptyList())
    override fun observePracticals(): Flow<List<Practical>> = flowOf(emptyList())
    override fun observeDocuments(): Flow<List<AcademicDocument>> = flowOf(emptyList())
    override fun observeDirectory(): Flow<List<DirectoryPerson>> = flowOf(emptyList())
    override fun observeNews(): Flow<List<NewsItem>> = flowOf(emptyList())
    override fun observeEvents(): Flow<List<StudentEvent>> = flowOf(emptyList())
    override suspend fun syncAll(force: Boolean, features: Set<SyncFeature>?) = Unit
    override suspend fun clearCache() {
        events += "clearCache"
    }
    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri = Uri.EMPTY
    override suspend fun joinGroup(courseId: String, projectId: String, groupId: String) {}
    override suspend fun leaveGroup(courseId: String, projectId: String, groupId: String) {}
}

private class RecordingSessionRepository(
    private val events: MutableList<String>
) : SessionRepository {
    override val session: Flow<Session?> = flowOf(null)
    override val hasLockedBiometricSession: Flow<Boolean> = flowOf(false)
    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit
    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) = Unit
    override suspend fun unlockWithBiometrics() = Unit
    override suspend fun logout() {
        events += "logout"
    }
}

private class RecordingNotificationScheduler(
    private val events: MutableList<String>
) : NotificationScheduler {
    override fun ensureChannels() = Unit
    override suspend fun scheduleStudentSync(intervalMinutes: Long) {
        events += "scheduleStudentSync"
    }
    override suspend fun runStudentSyncNow() = Unit
    override suspend fun cancelStudentSync() {
        events += "cancelSync"
    }
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) = Unit
    override suspend fun showNewAbsence(absence: Absence) = Unit
    override suspend fun showAgendaChange(event: AgendaEvent) = Unit
    override suspend fun showProjectDeadline(project: Project) = Unit
    override suspend fun showNewDocument(document: AcademicDocument) = Unit
    override suspend fun scheduleReminders(targets: List<ReminderTarget>, classLeadMinutes: Int, deadlineLeadMinutes: Int) = Unit
}
