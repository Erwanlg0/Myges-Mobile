package com.elg.myges.adapters.primary.viewmodel

import android.net.Uri
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.application.usecase.ClearCacheUseCase
import com.elg.myges.application.usecase.LogoutUseCase
import com.elg.myges.application.usecase.ObserveSettingsUseCase
import com.elg.myges.application.usecase.UpdateSettingsUseCase
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.NotificationPreferences
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.Session
import com.elg.myges.domain.model.UserSettings
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
            UpdateSettingsUseCase(settingsRepository),
            ClearCacheUseCase(studentDataRepository, settingsRepository),
            LogoutUseCase(sessionRepository, notificationScheduler)
        )
    }
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
    }

    override suspend fun setLanguageTag(languageTag: String?) {
        failure?.let { throw it }
        this.languageTag = languageTag
    }

    override suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        failure?.let { throw it }
    }

    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
    }

    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
    }

    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
    }

    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
    }

    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) {
        failure?.let { throw it }
        documentNotifications = enabled
    }

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
    override suspend fun syncAll() = Unit
    override suspend fun clearCache() {
        events += "clearCache"
    }
    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri = Uri.EMPTY
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
    override suspend fun scheduleStudentSync() = Unit
    override suspend fun cancelStudentSync() {
        events += "cancelSync"
    }
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) = Unit
    override suspend fun showNewAbsence(absence: Absence) = Unit
    override suspend fun showAgendaChange(event: AgendaEvent) = Unit
    override suspend fun showProjectDeadline(project: Project) = Unit
    override suspend fun showNewDocument(document: AcademicDocument) = Unit
}
