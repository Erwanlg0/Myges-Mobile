package com.elg.myges.adapters.primary.viewmodel

import android.net.Uri
import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.application.ports.NetworkMonitor
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.application.usecase.DownloadDocumentUseCase
import com.elg.myges.application.usecase.ObserveAbsencesUseCase
import com.elg.myges.application.usecase.ObserveAgendaUseCase
import com.elg.myges.application.usecase.ObserveCoursesUseCase
import com.elg.myges.application.usecase.ObserveDashboardUseCase
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
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.NotificationPreferences
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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

    private fun studentViewModel(
        repository: FakeStudentDataRepository,
        settingsRepository: FakeSettingsRepository,
        networkMonitor: FakeNetworkMonitor
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
            ObserveNewsUseCase(repository),
            RefreshStudentDataUseCase(repository, settingsRepository, calendarSyncPort),
            SyncAgendaToCalendarUseCase(calendarSyncPort),
            DownloadDocumentUseCase(repository),
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
    override fun observeNews(): Flow<List<NewsItem>> = news
    override suspend fun syncAll() {
        syncCount += 1
    }
    override suspend fun clearCache() = Unit
    override suspend fun downloadDocument(document: AcademicDocument): Uri {
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
    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun markSynced() = Unit
    override suspend fun clearSyncMetadata() = Unit
}

private class FakeCalendarSyncPort : CalendarSyncPort {
    override suspend fun sync(events: List<AgendaEvent>) = Unit
}
