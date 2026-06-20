package com.elg.studly.application.usecase

import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

class ObserverUseCasesTest {

    @Test
    fun observeSessionUseCaseReturnsFlow() = runTest {
        val repository = mockk<SessionRepository>()
        val expectedSession = Session("user", "token", null, kotlin.time.Clock.System.now(), false, kotlin.time.Clock.System.now(), kotlin.time.Clock.System.now())
        every { repository.session } returns MutableStateFlow(expectedSession)

        val useCase = ObserveSessionUseCase(repository)
        assertEquals(expectedSession, useCase().first())
    }

    @Test
    fun observeLockedBiometricSessionUseCaseReturnsFlow() = runTest {
        val repository = mockk<SessionRepository>()
        every { repository.hasLockedBiometricSession } returns MutableStateFlow(true)

        val useCase = ObserveLockedBiometricSessionUseCase(repository)
        assertEquals(true, useCase().first())
    }

    @Test
    fun observeSettingsUseCaseReturnsFlow() = runTest {
        val repository = mockk<SettingsRepository>()
        val settings = mockk<UserSettings>()
        every { repository.settings } returns MutableStateFlow(settings)

        val useCase = ObserveSettingsUseCase(repository)
        assertEquals(settings, useCase().first())
    }

    @Test
    fun observeDashboardUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val dashboard = mockk<DashboardSummary>()
        every { repository.observeDashboard() } returns MutableStateFlow(dashboard)

        val useCase = ObserveDashboardUseCase(repository)
        assertEquals(dashboard, useCase().first())
    }

    @Test
    fun observeAgendaUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<AgendaEvent>(mockk())
        every { repository.observeAgenda() } returns MutableStateFlow(list)

        val useCase = ObserveAgendaUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeGradesUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<Grade>(mockk())
        every { repository.observeGrades() } returns MutableStateFlow(list)

        val useCase = ObserveGradesUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeAbsencesUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<Absence>(mockk())
        every { repository.observeAbsences() } returns MutableStateFlow(list)

        val useCase = ObserveAbsencesUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeCoursesUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<Course>(mockk())
        every { repository.observeCourses() } returns MutableStateFlow(list)

        val useCase = ObserveCoursesUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeProjectsUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<Project>(mockk())
        every { repository.observeProjects() } returns MutableStateFlow(list)

        val useCase = ObserveProjectsUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observePracticalsUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<Practical>(mockk())
        every { repository.observePracticals() } returns MutableStateFlow(list)

        val useCase = ObservePracticalsUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeDocumentsUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<AcademicDocument>(mockk())
        every { repository.observeDocuments() } returns MutableStateFlow(list)

        val useCase = ObserveDocumentsUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeDirectoryUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<DirectoryPerson>(mockk())
        every { repository.observeDirectory() } returns MutableStateFlow(list)

        val useCase = ObserveDirectoryUseCase(repository)
        assertEquals(list, useCase().first())
    }

    @Test
    fun observeNewsUseCaseReturnsFlow() = runTest {
        val repository = mockk<StudentDataRepository>()
        val list = listOf<NewsItem>(mockk())
        every { repository.observeNews() } returns MutableStateFlow(list)

        val useCase = ObserveNewsUseCase(repository)
        assertEquals(list, useCase().first())
    }
}
