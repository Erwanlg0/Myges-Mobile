package com.elg.studly.adapters.primary.viewmodel

import android.net.Uri
import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.application.usecase.ClearCacheUseCase
import com.elg.studly.application.usecase.LogoutUseCase
import com.elg.studly.application.usecase.RescheduleSyncUseCase
import com.elg.studly.application.usecase.UpdateReminderLeadUseCase
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
        assertEquals(settingsRepository.settings.value, viewModel.state.value.settings)
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
        val sessionRepository = RecordingSessionRepository(mutableListOf())
        val viewModel = settingsViewModel(settingsRepository = settingsRepository, sessionRepository = sessionRepository)

        viewModel.setBiometricEnabled(true)
        advanceUntilIdle()

        assertEquals(true, settingsRepository.biometricEnabled)
        assertEquals(true, sessionRepository.biometricEnabled)
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
        viewModel.setAgendaColorMode(com.elg.studly.domain.model.AgendaColorMode.Location)
        advanceUntilIdle()

        assertEquals(com.elg.studly.domain.model.ThemeMode.Dark, settingsRepository.themeMode)
        assertEquals(true, settingsRepository.dynamicColorEnabled)
        assertEquals(com.elg.studly.domain.model.AgendaColorMode.Location, settingsRepository.agendaColorMode)
    }

    @Test
    fun setRefreshIntervalsUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setRefreshInterval(SyncFeature.Grades, 30)
        advanceUntilIdle()

        assertEquals(30, settingsRepository.refreshIntervals[SyncFeature.Grades])
        assertEquals(listOf("scheduleStudentSync"), settingsRepository.events)
    }

    @Test
    fun reminderLeadUpdatesSettingsRepository() = runTest(dispatcher) {
        val settingsRepository = RecordingSettingsRepository()
        val viewModel = settingsViewModel(settingsRepository = settingsRepository)

        viewModel.setClassReminderLead(15)
        viewModel.setDeadlineReminderLead(60)
        advanceUntilIdle()

        assertEquals(15, settingsRepository.classReminderLeadMinutes)
        assertEquals(60, settingsRepository.deadlineReminderLeadMinutes)
    }

    @Test
    fun loadAndSelectCalendarsDelegateToPort() = runTest(dispatcher) {
        val calendarSyncPort = RecordingCalendarSyncPort()
        val viewModel = settingsViewModel(calendarSyncPort = calendarSyncPort)

        viewModel.loadCalendars()
        advanceUntilIdle()
        viewModel.selectCalendar(2L)
        advanceUntilIdle()

        assertEquals(listOf(CalendarAccount(1L, "Primary", "account")), viewModel.calendars.value)
        assertEquals(2L, viewModel.selectedCalendarId.value)
        assertEquals(2L, calendarSyncPort.selected)
    }

    @Test
    fun connectCalendarSelectsThenSyncsBeforeEnabling() = runTest(dispatcher) {
        val events = mutableListOf<String>()
        val settingsRepository = RecordingSettingsRepository(events)
        val viewModel = settingsViewModel(
            settingsRepository = settingsRepository,
            calendarSyncPort = RecordingCalendarSyncPort(events)
        )

        viewModel.connectCalendar(2L, emptyList())
        advanceUntilIdle()

        assertEquals(listOf("selectCalendar", "syncCalendar", "enableCalendar"), events)
        assertEquals(true, settingsRepository.calendarSyncEnabled)
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
        notificationScheduler: RecordingNotificationScheduler = RecordingNotificationScheduler(settingsRepository.events),
        calendarSyncPort: CalendarSyncPort = RecordingCalendarSyncPort()
    ): SettingsViewModel {
        return SettingsViewModel(
            settingsRepository,
            sessionRepository,
            UpdateReminderLeadUseCase(settingsRepository, studentDataRepository, notificationScheduler),
            ClearCacheUseCase(studentDataRepository, settingsRepository),
            LogoutUseCase(sessionRepository, notificationScheduler, studentDataRepository, settingsRepository),
            calendarSyncPort,
            RescheduleSyncUseCase(settingsRepository, notificationScheduler)
        )
    }
}

private class RecordingCalendarSyncPort(
    private val events: MutableList<String> = mutableListOf()
) : CalendarSyncPort {
    var selected: Long? = 1L

    override suspend fun sync(events: List<AgendaEvent>) {
        this.events += "syncCalendar"
    }
    override suspend fun availableCalendars(): List<CalendarAccount> = listOf(CalendarAccount(1L, "Primary", "account"))
    override suspend fun selectedCalendarId(): Long? = selected
    override suspend fun selectCalendar(id: Long) {
        events += "selectCalendar"
        selected = id
    }
}

private class RecordingSettingsRepository(
    val events: MutableList<String> = mutableListOf()
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
        if (enabled) events += "enableCalendar"
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
    var agendaColorMode: com.elg.studly.domain.model.AgendaColorMode? = null
    var dynamicColorEnabled: Boolean? = null
    var calendarSyncEnabled: Boolean? = null
    var biometricEnabled: Boolean? = null
    var gradeNotifications: Boolean? = null
    var absenceNotifications: Boolean? = null
    var agendaNotifications: Boolean? = null
    var projectNotifications: Boolean? = null
    var classReminderLeadMinutes: Int? = null
    var deadlineReminderLeadMinutes: Int? = null
    val refreshIntervals = mutableMapOf<SyncFeature, Int>()

    override suspend fun setThemeMode(themeMode: com.elg.studly.domain.model.ThemeMode) {
        this.themeMode = themeMode
    }
    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        this.dynamicColorEnabled = enabled
    }
    override suspend fun setAgendaColorMode(mode: com.elg.studly.domain.model.AgendaColorMode) {
        this.agendaColorMode = mode
    }
    override suspend fun setRefreshInterval(feature: SyncFeature, minutes: Int) {
        refreshIntervals[feature] = minutes
    }
    override suspend fun setClassReminderLeadMinutes(minutes: Int) {
        classReminderLeadMinutes = minutes
    }
    override suspend fun setDeadlineReminderLeadMinutes(minutes: Int) {
        deadlineReminderLeadMinutes = minutes
    }
    override suspend fun lastFetchedAt(feature: SyncFeature): Instant? = null
    override suspend fun markFeatureFetched(feature: SyncFeature) = Unit

    override suspend fun markSynced() = Unit

    override suspend fun clearSyncMetadata() {
        events += "clearSyncMetadata"
    }

    override suspend fun setShowGradeLetters(enabled: Boolean) {
        val current = settings.value
        settings.value = current.copy(showGradeLetters = enabled)
    }

    override suspend fun setEstimateGrades(enabled: Boolean) {
        val current = settings.value
        settings.value = current.copy(estimateGrades = enabled)
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
    override suspend fun subscribeEvent(eventId: String) {}
    override suspend fun unsubscribeEvent(eventId: String) {}
    override suspend fun projectMessages(groupId: String) = emptyList<com.elg.studly.domain.model.ProjectMessage>()
    override suspend fun sendProjectMessage(groupId: String, message: String) {}
}

private class RecordingSessionRepository(
    private val events: MutableList<String>
) : SessionRepository {
    var biometricEnabled: Boolean? = null
    override val session: Flow<Session?> = flowOf(null)
    override val hasLockedBiometricSession: Flow<Boolean> = flowOf(false)
    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit
    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) = Unit
    override suspend fun unlockWithBiometrics() = Unit
    override suspend fun setBiometricEnabled(enabled: Boolean) {
        biometricEnabled = enabled
    }
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
