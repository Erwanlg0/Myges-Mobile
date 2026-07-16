package com.elg.studly.adapters.primary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.usecase.ClearCacheUseCase
import com.elg.studly.application.usecase.LogoutUseCase
import com.elg.studly.application.usecase.RescheduleSyncUseCase
import com.elg.studly.application.usecase.UpdateReminderLeadUseCase
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.AgendaColorMode
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.CalendarAccount
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.UserSettings
import com.elg.studly.domain.model.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings?,
    val loading: Boolean,
    val error: AppError?
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val updateReminderLeadUseCase: UpdateReminderLeadUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val calendarSyncPort: CalendarSyncPort,
    private val rescheduleSyncUseCase: RescheduleSyncUseCase
) : ViewModel() {
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)
    private var connectingCalendar = false

    private val _calendars = MutableStateFlow<List<CalendarAccount>>(emptyList())
    val calendars: StateFlow<List<CalendarAccount>> = _calendars

    private val _selectedCalendarId = MutableStateFlow<Long?>(null)
    val selectedCalendarId: StateFlow<Long?> = _selectedCalendarId

    fun loadCalendars() {
        viewModelScope.launch {
            runCatching {
                _calendars.value = calendarSyncPort.availableCalendars()
                _selectedCalendarId.value = calendarSyncPort.selectedCalendarId()
            }.onFailure { error.value = it.toAppError() }
        }
    }

    fun selectCalendar(id: Long) {
        viewModelScope.launch {
            runCatching {
                calendarSyncPort.selectCalendar(id)
                _selectedCalendarId.value = id
            }.onFailure { error.value = it.toAppError() }
        }
    }

    fun connectCalendar(id: Long, events: List<AgendaEvent>) {
        if (connectingCalendar) return
        connectingCalendar = true
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching {
                calendarSyncPort.selectCalendar(id)
                _selectedCalendarId.value = id
                calendarSyncPort.sync(events)
                settingsRepository.setCalendarSyncEnabled(true)
            }.onFailure { error.value = it.toAppError() }
            loading.value = false
            connectingCalendar = false
        }
    }

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        loading,
        error
    ) { settings, isLoading, currentError ->
        SettingsUiState(settings, isLoading, currentError)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState(null, false, null))

    fun setLanguage(languageTag: String?) = launchSettingChange {
        settingsRepository.setLanguageTag(languageTag)
    }

    fun setThemeMode(themeMode: ThemeMode) = launchSettingChange {
        settingsRepository.setThemeMode(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) = launchSettingChange {
        settingsRepository.setDynamicColorEnabled(enabled)
    }

    fun setAgendaColorMode(mode: AgendaColorMode) = launchSettingChange {
        settingsRepository.setAgendaColorMode(mode)
    }

    fun setCalendarSync(enabled: Boolean) = launchSettingChange {
        settingsRepository.setCalendarSyncEnabled(enabled)
    }

    fun setBiometricEnabled(enabled: Boolean) = launchSettingChange {
        sessionRepository.setBiometricEnabled(enabled)
        settingsRepository.setBiometricEnabled(enabled)
    }

    fun setGradeNotifications(enabled: Boolean) = launchSettingChange {
        settingsRepository.setGradeNotificationsEnabled(enabled)
    }

    fun setAbsenceNotifications(enabled: Boolean) = launchSettingChange {
        settingsRepository.setAbsenceNotificationsEnabled(enabled)
    }

    fun setAgendaNotifications(enabled: Boolean) = launchSettingChange {
        settingsRepository.setAgendaNotificationsEnabled(enabled)
    }

    fun setProjectNotifications(enabled: Boolean) = launchSettingChange {
        settingsRepository.setProjectNotificationsEnabled(enabled)
    }

    fun setDocumentNotifications(enabled: Boolean) = launchSettingChange {
        settingsRepository.setDocumentNotificationsEnabled(enabled)
    }

    fun setRefreshInterval(feature: SyncFeature, minutes: Int) = launchSettingChange {
        settingsRepository.setRefreshInterval(feature, minutes)
        rescheduleSyncUseCase()
    }

    fun setClassReminderLead(minutes: Int) = launchSettingChange {
        updateReminderLeadUseCase.classReminderLead(minutes)
    }

    fun setDeadlineReminderLead(minutes: Int) = launchSettingChange {
        updateReminderLeadUseCase.deadlineReminderLead(minutes)
    }

    fun clearCache() = launchSettingChange {
        clearCacheUseCase()
    }

    fun logout() = launchSettingChange {
        logoutUseCase()
    }

    fun setShowGradeLetters(enabled: Boolean) = launchSettingChange {
        settingsRepository.setShowGradeLetters(enabled)
    }

    fun setEstimateGrades(enabled: Boolean) = launchSettingChange {
        settingsRepository.setEstimateGrades(enabled)
    }

    private fun launchSettingChange(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { block() }
                .onFailure { error.value = it.toAppError() }
            loading.value = false
        }
    }
}
