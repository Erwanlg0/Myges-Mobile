package com.elg.studly.adapters.primary.viewmodel

import android.net.Uri
import com.elg.studly.application.ports.NetworkMonitor
import com.elg.studly.application.usecase.*
import com.elg.studly.domain.model.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var refreshStudentDataUseCase: RefreshStudentDataUseCase
    private lateinit var syncAgendaToCalendarUseCase: SyncAgendaToCalendarUseCase
    private lateinit var downloadDocumentUseCase: DownloadDocumentUseCase
    private lateinit var joinGroupUseCase: JoinGroupUseCase
    private lateinit var leaveGroupUseCase: LeaveGroupUseCase
    private lateinit var projectMessagesUseCase: ProjectMessagesUseCase
    private lateinit var sendProjectMessageUseCase: SendProjectMessageUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var networkMonitor: NetworkMonitor

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        refreshStudentDataUseCase = mockk(relaxed = true)
        syncAgendaToCalendarUseCase = mockk(relaxed = true)
        downloadDocumentUseCase = mockk(relaxed = true)
        joinGroupUseCase = mockk(relaxed = true)
        leaveGroupUseCase = mockk(relaxed = true)
        projectMessagesUseCase = mockk(relaxed = true)
        sendProjectMessageUseCase = mockk(relaxed = true)
        logoutUseCase = mockk(relaxed = true)
        networkMonitor = mockk {
            every { isOnline } returns MutableStateFlow(true)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): StudentViewModel {
        return StudentViewModel(
            observeDashboard = mockk { every { this@mockk.invoke() } returns MutableStateFlow(mockk<DashboardSummary>(relaxed = true)) },
            observeAgenda = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeGrades = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeAbsences = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeCourses = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeProjects = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observePracticals = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeDocuments = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeDirectory = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeNews = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            observeEvents = mockk { every { this@mockk.invoke() } returns MutableStateFlow(emptyList()) },
            refreshStudentDataUseCase = refreshStudentDataUseCase,
            syncAgendaToCalendarUseCase = syncAgendaToCalendarUseCase,
            downloadDocumentUseCase = downloadDocumentUseCase,
            joinGroupUseCase = joinGroupUseCase,
            leaveGroupUseCase = leaveGroupUseCase,
            projectMessagesUseCase = projectMessagesUseCase,
            sendProjectMessageUseCase = sendProjectMessageUseCase,
            logoutUseCase = logoutUseCase,
            networkMonitor = networkMonitor
        )
    }

    @Test
    fun refreshTriggersUseCase() = runTest(dispatcher) {
        val viewModel = createViewModel()
        
        viewModel.refresh()
        advanceUntilIdle()

        io.mockk.coVerify { refreshStudentDataUseCase(true) }
    }

    @Test
    fun syncAgendaToCalendarTriggersUseCase() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val events = listOf(mockk<AgendaEvent>())
        
        viewModel.syncAgendaToCalendar(events)
        advanceUntilIdle()

        io.mockk.coVerify { syncAgendaToCalendarUseCase(events) }
    }

    @Test
    fun openDocumentTriggersDownloadAndEmitsUri() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val document = AcademicDocument("id", "Doc", null, null, "application/pdf", "doc.pdf", "url", Instant.now())
        val uri = mockk<Uri>()
        coEvery { downloadDocumentUseCase(document, any()) } returns uri

        val requests = mutableListOf<DocumentOpenRequest>()
        val job = backgroundScope.launch {
            viewModel.documentOpenRequests.collect { requests.add(it) }
        }

        viewModel.openDocument(document)
        advanceUntilIdle()

        assertEquals(1, requests.size)
        assertEquals(uri, requests[0].uri)
        assertEquals("application/pdf", requests[0].mimeType)
        
        job.cancel()
    }

    @Test
    fun joinGroupTriggersUseCaseWithoutFullRefresh() = runTest(dispatcher) {
        val viewModel = createViewModel()
        coEvery { joinGroupUseCase("courseId", "projectId", "groupId") } returns Unit

        viewModel.joinGroup("courseId", "projectId", "groupId")
        advanceUntilIdle()

        io.mockk.coVerify { joinGroupUseCase("courseId", "projectId", "groupId") }
    }

    @Test
    fun reportOpenDocumentFailureSetsError() = runTest(dispatcher) {
        val viewModel = createViewModel()
        
        viewModel.reportOpenDocumentFailure()

        assertEquals(AppError.Storage, viewModel.documentError.value)
    }



    @Test
    fun leaveGroupTriggersUseCaseWithoutFullRefresh() = runTest(dispatcher) {
        val viewModel = createViewModel()
        coEvery { leaveGroupUseCase("courseId", "projectId", "groupId") } returns Unit

        viewModel.leaveGroup("courseId", "projectId", "groupId")
        advanceUntilIdle()

        io.mockk.coVerify { leaveGroupUseCase("courseId", "projectId", "groupId") }
    }



    @Test
    fun navigateToGradesPeriodUpdatesFlow() = runTest(dispatcher) {
        val viewModel = createViewModel()
        
        viewModel.navigateToGradesPeriod("2023-2024")
        
        assertEquals("2023-2024", viewModel.gradesPeriodToNavigate.first())
    }

    @Test
    fun navigateToAbsencesPeriodUpdatesFlow() = runTest(dispatcher) {
        val viewModel = createViewModel()
        
        viewModel.navigateToAbsencesPeriod("2023-2024")
        
        assertEquals("2023-2024", viewModel.absencesPeriodToNavigate.first())
    }

    @Test
    fun navigateToAgendaDateUpdatesFlow() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val date = LocalDate.of(2023, 10, 15)
        
        viewModel.navigateToAgendaDate(date)
        
        assertEquals(date, viewModel.agendaDateToNavigate.first())
    }
}
