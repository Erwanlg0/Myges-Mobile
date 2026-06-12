package com.elg.myges.adapters.primary.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.myges.adapters.primary.state.FeatureUiState
import com.elg.myges.application.ports.NetworkMonitor
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
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    observeNews: ObserveNewsUseCase,
    private val refreshStudentDataUseCase: RefreshStudentDataUseCase,
    private val syncAgendaToCalendarUseCase: SyncAgendaToCalendarUseCase,
    private val downloadDocumentUseCase: DownloadDocumentUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)
    val documentOpenRequests = MutableSharedFlow<DocumentOpenRequest>()

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

    val news: StateFlow<FeatureUiState<List<NewsItem>>> = observeNews()
        .asFeatureState(emptyList(), networkMonitor.isOnline)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            runCatching { refreshStudentDataUseCase() }
                .onFailure { error.value = it.toAppError() }
            refreshing.value = false
        }
    }

    fun syncAgendaToCalendar(events: List<AgendaEvent>) {
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            runCatching { syncAgendaToCalendarUseCase(events) }
                .onFailure { error.value = it.toAppError() }
            refreshing.value = false
        }
    }

    fun openDocument(document: AcademicDocument) {
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            runCatching { downloadDocumentUseCase(document) }
                .onSuccess { uri -> documentOpenRequests.emit(DocumentOpenRequest(uri, document.mimeType)) }
                .onFailure { error.value = it.toAppError() }
            refreshing.value = false
        }
    }

    fun reportOpenDocumentFailure() {
        error.value = AppError.Storage
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
