package com.elg.myges.adapters.primary.viewmodel

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.myges.adapters.primary.state.FeatureUiState
import java.time.LocalDate
import com.elg.myges.application.ports.NetworkMonitor
import com.elg.myges.application.usecase.DownloadDocumentUseCase
import com.elg.myges.application.usecase.LogoutUseCase
import com.elg.myges.application.usecase.ObserveAbsencesUseCase
import com.elg.myges.application.usecase.ObserveAgendaUseCase
import com.elg.myges.application.usecase.ObserveCoursesUseCase
import com.elg.myges.application.usecase.ObserveDirectoryUseCase
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
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentOpenRequest(
    val uri: Uri,
    val mimeType: String?
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    observeAgenda: ObserveAgendaUseCase,
    observeGrades: ObserveGradesUseCase,
    observeAbsences: ObserveAbsencesUseCase,
    observeCourses: ObserveCoursesUseCase,
    observeProjects: ObserveProjectsUseCase,
    observePracticals: ObservePracticalsUseCase,
    observeDocuments: ObserveDocumentsUseCase,
    observeDirectory: ObserveDirectoryUseCase,
    observeNews: ObserveNewsUseCase,
    private val refreshStudentDataUseCase: RefreshStudentDataUseCase,
    private val syncAgendaToCalendarUseCase: SyncAgendaToCalendarUseCase,
    private val downloadDocumentUseCase: DownloadDocumentUseCase,
    private val logoutUseCase: LogoutUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)
    private val _downloadingDocumentIds = MutableStateFlow(emptySet<String>())
    private val _documentDownloadProgress = MutableStateFlow(emptyMap<String, Float?>())
    val downloadingDocumentIds: StateFlow<Set<String>> = _downloadingDocumentIds
    val documentDownloadProgress: StateFlow<Map<String, Float?>> = _documentDownloadProgress
    val documentOpenRequests = MutableSharedFlow<DocumentOpenRequest>()
    private val _refreshSucceeded = MutableSharedFlow<Unit>()
    val refreshSucceeded: SharedFlow<Unit> = _refreshSucceeded
    private val _calendarSyncCompleted = MutableSharedFlow<Unit>()
    val calendarSyncCompleted: SharedFlow<Unit> = _calendarSyncCompleted

    val agendaDateToNavigate = MutableSharedFlow<LocalDate>()
    val gradesPeriodToNavigate = MutableSharedFlow<String>()
    val absencesPeriodToNavigate = MutableSharedFlow<String>()

    fun navigateToAgendaDate(date: LocalDate) {
        viewModelScope.launch {
            agendaDateToNavigate.emit(date)
        }
    }

    fun navigateToGradesPeriod(period: String) {
        viewModelScope.launch {
            gradesPeriodToNavigate.emit(period)
        }
    }

    fun navigateToAbsencesPeriod(period: String) {
        viewModelScope.launch {
            absencesPeriodToNavigate.emit(period)
        }
    }

    val dashboard: StateFlow<FeatureUiState<DashboardSummary?>> = observeDashboard()
        .asFeatureState(null, networkMonitor.isOnline)

    val agenda: StateFlow<FeatureUiState<List<AgendaEvent>>> = observeAgenda()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val grades: StateFlow<FeatureUiState<List<Grade>>> = observeGrades()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val absences: StateFlow<FeatureUiState<List<Absence>>> = observeAbsences()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val courses: StateFlow<FeatureUiState<List<Course>>> = observeCourses()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val projects: StateFlow<FeatureUiState<List<Project>>> = observeProjects()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val practicals: StateFlow<FeatureUiState<List<Practical>>> = observePracticals()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val documents: StateFlow<FeatureUiState<List<AcademicDocument>>> = observeDocuments()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val directory: StateFlow<FeatureUiState<List<DirectoryPerson>>> = observeDirectory()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    val news: StateFlow<FeatureUiState<List<NewsItem>>> = observeNews()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    init {
        refresh()
        observeNetworkRecovery(networkMonitor.isOnline)
    }

    fun refresh() {
        viewModelScope.launch {
            if (refreshing.value) return@launch
            refreshing.value = true
            error.value = null
            runCatching { refreshStudentDataUseCase() }
                .onSuccess { _refreshSucceeded.emit(Unit) }
                .onFailure { handleFailure(it) }
            refreshing.value = false
        }
    }

    fun syncAgendaToCalendar(events: List<AgendaEvent>) {
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            runCatching { syncAgendaToCalendarUseCase(events) }
                .onSuccess { _calendarSyncCompleted.emit(Unit) }
                .onFailure { handleFailure(it) }
            refreshing.value = false
        }
    }

    fun openDocument(document: AcademicDocument) {
        viewModelScope.launch {
            _downloadingDocumentIds.update { it + document.id }
            _documentDownloadProgress.update { it + (document.id to null) }
            error.value = null
            runCatching {
                downloadDocumentUseCase(document) { progress ->
                    _documentDownloadProgress.update { it + (document.id to progress) }
                }
            }
                .onSuccess { uri -> documentOpenRequests.emit(DocumentOpenRequest(uri, document.resolvedMimeType())) }
                .onFailure { handleFailure(it) }
            _downloadingDocumentIds.update { it - document.id }
            _documentDownloadProgress.update { it - document.id }
        }
    }

    fun reportOpenDocumentFailure() {
        error.value = AppError.Storage
    }

    private suspend fun handleFailure(throwable: Throwable) {
        val appError = throwable.toAppError()
        error.value = appError
        if (appError == AppError.Unauthorized) {
            runCatching { logoutUseCase() }
        }
    }

    private fun observeNetworkRecovery(online: Flow<Boolean>) {
        viewModelScope.launch {
            var previousOnline: Boolean? = null
            online.distinctUntilChanged().collect { isOnline ->
                val wasOnline = previousOnline
                previousOnline = isOnline
                if (wasOnline == false && isOnline) {
                    refresh()
                }
            }
        }
    }

    private fun <T> Flow<T>.asFeatureState(initialValue: T, online: Flow<Boolean>): StateFlow<FeatureUiState<T>> {
        return combine(this, refreshing, error, online) { data, isRefreshing, currentError, isOnline ->
            FeatureUiState(
                data = data,
                loading = false,
                refreshing = isRefreshing,
                error = currentError,
                online = isOnline
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeatureUiState(initialValue)
        )
    }
}

private fun AcademicDocument.resolvedMimeType(): String? {
    mimeType?.takeIf { it.isNotBlank() }?.let { return it }
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
        .takeIf { it.isNotBlank() }
    return extension?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.lowercase()) }
}
