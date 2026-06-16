package com.elg.myges.adapters.primary.viewmodel

import app.cash.turbine.test
import android.net.Uri
import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.application.ports.NetworkMonitor
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.application.usecase.DownloadDocumentUseCase
import com.elg.myges.application.usecase.LogoutUseCase
import com.elg.myges.application.usecase.ObserveAbsencesUseCase
import com.elg.myges.application.usecase.ObserveAgendaUseCase
import com.elg.myges.application.usecase.ObserveCoursesUseCase
import com.elg.myges.application.usecase.ObserveDashboardUseCase
import com.elg.myges.application.usecase.ObserveDirectoryUseCase
import com.elg.myges.application.usecase.ObserveDocumentsUseCase
import com.elg.myges.application.usecase.ObserveGradesUseCase
import com.elg.myges.application.usecase.ObserveNewsUseCase
import com.elg.myges.application.usecase.ObservePracticalsUseCase
import com.elg.myges.application.usecase.ObserveProjectsUseCase
import com.elg.myges.application.usecase.RefreshStudentDataUseCase
import com.elg.myges.application.usecase.SyncAgendaToCalendarUseCase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelNetworkRecoveryTest {
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
    fun refreshesWhenNetworkReturnsAfterOfflineState() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        val settingsRepository = FakeSettingsRepository()
        val networkMonitor = FakeNetworkMonitor(false)
        studentViewModel(repository, settingsRepository, networkMonitor)
        advanceUntilIdle()

        assertEquals(1, repository.syncCount)

        networkMonitor.online.value = true
        advanceUntilIdle()

        assertEquals(2, repository.syncCount)
    }

    @Test
    fun initialOnlineStateDoesNotTriggerDuplicateRefresh() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        val settingsRepository = FakeSettingsRepository()
        studentViewModel(repository, settingsRepository, FakeNetworkMonitor(true))
        advanceUntilIdle()

        assertEquals(1, repository.syncCount)
    }

    @Test
    fun openDocumentTracksOnlyTheDownloadingDocument() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        val settingsRepository = FakeSettingsRepository()
        val viewModel = studentViewModel(repository, settingsRepository, FakeNetworkMonitor(true))
        val document = AcademicDocument(
            id = "document-1",
            title = "Certificate",
            category = null,
            year = null,
            mimeType = "application/pdf",
            fileName = "certificate.pdf",
            downloadUrl = "https://example.com/certificate.pdf",
            updatedAt = null
        )
        repository.downloadFinished = CompletableDeferred()
        advanceUntilIdle()

        viewModel.openDocument(document)
        advanceUntilIdle()

        assertTrue("document-1" in viewModel.downloadingDocumentIds.value)

        repository.downloadFinished?.complete(Unit)
        advanceUntilIdle()

        assertFalse("document-1" in viewModel.downloadingDocumentIds.value)
    }

    @Test
    fun openDocumentExposesDownloadProgress() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        val settingsRepository = FakeSettingsRepository()
        val viewModel = studentViewModel(repository, settingsRepository, FakeNetworkMonitor(true))
        val document = AcademicDocument(
            id = "document-1",
            title = "Certificate",
            category = null,
            year = null,
            mimeType = "application/pdf",
            fileName = "certificate.pdf",
            downloadUrl = "https://example.com/certificate.pdf",
            updatedAt = null
        )
        repository.downloadFinished = CompletableDeferred()
        advanceUntilIdle()

        viewModel.openDocument(document)
        advanceUntilIdle()

        assertEquals(0.5f, viewModel.documentDownloadProgress.value["document-1"])

        repository.downloadFinished?.complete(Unit)
        advanceUntilIdle()

        assertFalse("document-1" in viewModel.documentDownloadProgress.value)
    }


    @Test
    fun unauthorizedRefreshLogsOutSessionAndCancelsSync() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        val settingsRepository = FakeSettingsRepository()
        val sessionRepository = FakeSessionRepository()
        val notificationScheduler = FakeNotificationScheduler()
        repository.syncFailure = AppException(AppError.Unauthorized)

        studentViewModel(
            repository = repository,
            settingsRepository = settingsRepository,
            networkMonitor = FakeNetworkMonitor(true),
            sessionRepository = sessionRepository,
            notificationScheduler = notificationScheduler
        )
        advanceUntilIdle()

        assertTrue(sessionRepository.loggedOut)
        assertTrue(notificationScheduler.syncCancelled)
    }

    @Test
    fun refreshSuccessSignalIsOnlyEmittedAfterSuccessfulSync() = runTest(dispatcher) {
        val repository = FakeStudentDataRepository()
        repository.syncFailure = IllegalStateException("sync failed")
        val viewModel = studentViewModel(repository, FakeSettingsRepository(), FakeNetworkMonitor(true))
        viewModel.refreshSucceeded.test {
            advanceUntilIdle()
            expectNoEvents()

            repository.syncFailure = null
            viewModel.refresh()
            advanceUntilIdle()

            awaitItem()
            expectNoEvents()
        }
    }

    private fun studentViewModel(
        repository: FakeStudentDataRepository,
        settingsRepository: FakeSettingsRepository,
        networkMonitor: FakeNetworkMonitor,
        sessionRepository: FakeSessionRepository = FakeSessionRepository(),
        notificationScheduler: FakeNotificationScheduler = FakeNotificationScheduler()
    ): StudentViewModel {
        val calendarSyncPort = FakeCalendarSyncPort()
        return StudentViewModel(
            ObserveDashboardUseCase(repository),
            ObserveAgendaUseCase(repository),
            ObserveGradesUseCase(repository),
            ObserveAbsencesUseCase(repository),
            ObserveCoursesUseCase(repository),
            ObserveProjectsUseCase(repository),
            ObservePracticalsUseCase(repository),
            ObserveDocumentsUseCase(repository),
            ObserveDirectoryUseCase(repository),
            ObserveNewsUseCase(repository),
            RefreshStudentDataUseCase(repository, settingsRepository, calendarSyncPort),
            SyncAgendaToCalendarUseCase(calendarSyncPort),
            DownloadDocumentUseCase(repository),
            LogoutUseCase(sessionRepository, notificationScheduler),
            networkMonitor
        )
    }
}

private class FakeNetworkMonitor(initialOnline: Boolean) : NetworkMonitor {
    val online = MutableStateFlow(initialOnline)
    override val isOnline: Flow<Boolean> = online
}

private class FakeStudentDataRepository : StudentDataRepository {
    var syncCount = 0
    var syncFailure: Throwable? = null
    var downloadFinished: CompletableDeferred<Unit>? = null
    private val dashboard = MutableStateFlow(DashboardSummary(null, null, emptyList(), emptyList(), emptyList(), null))
    private val agenda = MutableStateFlow(emptyList<AgendaEvent>())
    private val grades = MutableStateFlow(emptyList<Grade>())
    private val absences = MutableStateFlow(emptyList<Absence>())
    private val courses = MutableStateFlow(emptyList<Course>())
    private val projects = MutableStateFlow(emptyList<Project>())
    private val practicals = MutableStateFlow(emptyList<Practical>())
    private val documents = MutableStateFlow(emptyList<AcademicDocument>())
    private val news = MutableStateFlow(emptyList<NewsItem>())

    override fun observeDashboard(): Flow<DashboardSummary> = dashboard
    override fun observeAgenda(): Flow<List<AgendaEvent>> = agenda
    override fun observeGrades(): Flow<List<Grade>> = grades
    override fun observeAbsences(): Flow<List<Absence>> = absences
    override fun observeCourses(): Flow<List<Course>> = courses
    override fun observeProjects(): Flow<List<Project>> = projects
    override fun observePracticals(): Flow<List<Practical>> = practicals
    override fun observeDocuments(): Flow<List<AcademicDocument>> = documents
    override fun observeDirectory(): Flow<List<DirectoryPerson>> = flowOf(emptyList())
    override fun observeNews(): Flow<List<NewsItem>> = news
    override suspend fun syncAll() {
        syncFailure?.let { throw it }
        syncCount += 1
    }
    override suspend fun clearCache() = Unit
    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri {
        onProgress(0.5f)
        downloadFinished?.await()
        return Uri.EMPTY
    }
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(
        UserSettings(
            languageTag = null,
            notifications = NotificationPreferences(
                grades = true,
                absences = true,
                agenda = true,
                projects = true,
                documents = true
            ),
            calendarSyncEnabled = false,
            lastSyncAt = null
        )
    )

    override suspend fun setLanguageTag(languageTag: String?) = Unit
    override suspend fun setCalendarSyncEnabled(enabled: Boolean) = Unit
    override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setThemeMode(themeMode: com.elg.myges.domain.model.ThemeMode) = Unit
    override suspend fun markSynced() = Unit
    override suspend fun clearSyncMetadata() = Unit
}

private class FakeCalendarSyncPort : CalendarSyncPort {
    override suspend fun sync(events: List<AgendaEvent>) = Unit
    override suspend fun availableCalendars(): List<com.elg.myges.domain.model.CalendarAccount> = emptyList()
    override suspend fun selectedCalendarId(): Long? = null
    override suspend fun selectCalendar(id: Long) = Unit
}

private class FakeSessionRepository : SessionRepository {
    var loggedOut = false
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override val hasLockedBiometricSession: StateFlow<Boolean> = MutableStateFlow(false)

    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit
    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) = Unit
    override suspend fun unlockWithBiometrics() = Unit
    override suspend fun logout() {
        loggedOut = true
    }
}

private class FakeNotificationScheduler : NotificationScheduler {
    var syncCancelled = false

    override fun ensureChannels() = Unit
    override suspend fun scheduleStudentSync() = Unit
    override suspend fun runStudentSyncNow() = Unit
    override suspend fun cancelStudentSync() {
        syncCancelled = true
    }
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) = Unit
    override suspend fun showNewAbsence(absence: Absence) = Unit
    override suspend fun showAgendaChange(event: AgendaEvent) = Unit
    override suspend fun showProjectDeadline(project: Project) = Unit
    override suspend fun showNewDocument(document: AcademicDocument) = Unit
}
