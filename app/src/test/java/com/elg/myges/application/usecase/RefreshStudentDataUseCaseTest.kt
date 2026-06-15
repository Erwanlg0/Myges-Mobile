package com.elg.myges.application.usecase

import android.net.Uri
import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.NotificationPreferences
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class RefreshStudentDataUseCaseTest {
    @Test
    fun refreshSyncsDataBeforeMarkingSettingsSynced() = runTest {
        val events = mutableListOf<String>()
        val repository = RecordingStudentDataRepository(events)
        val settingsRepository = RecordingSettingsRepository(events)
        val calendarSyncPort = RecordingCalendarSyncPort(events)
        val useCase = RefreshStudentDataUseCase(repository, settingsRepository, calendarSyncPort)

        useCase()

        assertEquals(listOf("sync", "markSynced"), events)
    }

    @Test
    fun refreshSyncsCalendarWhenPreferenceIsEnabled() = runTest {
        val events = mutableListOf<String>()
        val repository = RecordingStudentDataRepository(events)
        val settingsRepository = RecordingSettingsRepository(events, calendarSyncEnabled = true)
        val calendarSyncPort = RecordingCalendarSyncPort(events)
        val useCase = RefreshStudentDataUseCase(repository, settingsRepository, calendarSyncPort)
        val agenda = listOf(sampleAgendaEvent())
        repository.agenda.value = agenda

        useCase()

        assertEquals(listOf("sync", "calendar:1", "markSynced"), events)
        assertEquals(agenda, calendarSyncPort.syncedEvents)
    }

    @Test
    fun clearCacheClearsLocalDataBeforeSyncMetadata() = runTest {
        val events = mutableListOf<String>()
        val repository = RecordingStudentDataRepository(events)
        val settingsRepository = RecordingSettingsRepository(events)
        val useCase = ClearCacheUseCase(repository, settingsRepository)

        useCase()

        assertEquals(listOf("clearCache", "clearSyncMetadata"), events)
    }
}

private class RecordingStudentDataRepository(
    private val events: MutableList<String>
) : StudentDataRepository {
    val agenda = MutableStateFlow(emptyList<AgendaEvent>())

    override fun observeDashboard(): Flow<DashboardSummary> = flowOf(DashboardSummary(null, null, emptyList(), emptyList(), emptyList(), null))
    override fun observeAgenda(): Flow<List<AgendaEvent>> = agenda
    override fun observeGrades(): Flow<List<Grade>> = flowOf(emptyList())
    override fun observeAbsences(): Flow<List<Absence>> = flowOf(emptyList())
    override fun observeCourses(): Flow<List<Course>> = flowOf(emptyList())
    override fun observeProjects(): Flow<List<Project>> = flowOf(emptyList())
    override fun observePracticals(): Flow<List<Practical>> = flowOf(emptyList())
    override fun observeDocuments(): Flow<List<AcademicDocument>> = flowOf(emptyList())
    override fun observeDirectory(): Flow<List<DirectoryPerson>> = flowOf(emptyList())
    override fun observeNews(): Flow<List<NewsItem>> = flowOf(emptyList())

    override suspend fun syncAll() {
        events += "sync"
    }

    override suspend fun clearCache() {
        events += "clearCache"
    }
    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri = Uri.EMPTY
}

private class RecordingSettingsRepository(
    private val events: MutableList<String>,
    calendarSyncEnabled: Boolean = false
) : SettingsRepository {
    override val settings: Flow<UserSettings> = MutableStateFlow(
        UserSettings(
            languageTag = null,
            notifications = NotificationPreferences(true, true, true, true, true),
            calendarSyncEnabled = calendarSyncEnabled,
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

    override suspend fun markSynced() {
        events += "markSynced"
    }

    override suspend fun clearSyncMetadata() {
        events += "clearSyncMetadata"
    }
}

private class RecordingCalendarSyncPort(
    private val events: MutableList<String>
) : CalendarSyncPort {
    var syncedEvents = emptyList<AgendaEvent>()

    override suspend fun sync(events: List<AgendaEvent>) {
        syncedEvents = events
        this.events += "calendar:${events.size}"
    }
}

private fun sampleAgendaEvent(): AgendaEvent {
    val startsAt = Instant.parse("2026-06-12T08:00:00Z")
    return AgendaEvent(
        id = "event-1",
        title = "Math",
        startsAt = startsAt,
        endsAt = startsAt.plusSeconds(3600),
        room = "A101",
        teacher = "Teacher",
        type = "Course",
        modality = "Présentiel",
        courseId = "course-1"
    )
}
