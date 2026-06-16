package com.elg.studly.adapters.primary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.studly.application.usecase.CalendarAccountsUseCase
import com.elg.studly.application.usecase.ClearCacheUseCase
import com.elg.studly.application.usecase.LogoutUseCase
import com.elg.studly.application.usecase.ObserveSettingsUseCase
import com.elg.studly.application.usecase.UpdateSettingsUseCase
import com.elg.studly.application.usecase.RescheduleSyncUseCase
import com.elg.studly.domain.model.SyncFeature
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
    observeSettings: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val calendarAccountsUseCase: CalendarAccountsUseCase,
    private val rescheduleSyncUseCase: RescheduleSyncUseCase
) : ViewModel() {
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)

    private val _calendars = MutableStateFlow<List<CalendarAccount>>(emptyList())
    val calendars: StateFlow<List<CalendarAccount>> = _calendars

    private val _selectedCalendarId = MutableStateFlow<Long?>(null)
    val selectedCalendarId: StateFlow<Long?> = _selectedCalendarId

    fun loadCalendars() {
        viewModelScope.launch {
            runCatching {
                _calendars.value = calendarAccountsUseCase.available()
                _selectedCalendarId.value = calendarAccountsUseCase.selected()
            }.onFailure { error.value = it.toAppError() }
        }
    }

    fun selectCalendar(id: Long) {
        viewModelScope.launch {
            runCatching {
                calendarAccountsUseCase.select(id)
                _selectedCalendarId.value = id
            }.onFailure { error.value = it.toAppError() }
        }
    }

    val state: StateFlow<SettingsUiState> = combine(
        observeSettings(),
        loading,
        error
    ) { settings, isLoading, currentError ->
        SettingsUiState(settings, isLoading, currentError)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState(null, false, null))

    fun setLanguage(languageTag: String?) = launchSettingChange {
        updateSettingsUseCase.language(languageTag)
    }

    fun setThemeMode(themeMode: ThemeMode) = launchSettingChange {
        updateSettingsUseCase.themeMode(themeMode)
    }

    fun setCalendarSync(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.calendarSync(enabled)
    }

    fun setBiometricEnabled(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.biometric(enabled)
    }

    fun setGradeNotifications(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.gradeNotifications(enabled)
    }

    fun setAbsenceNotifications(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.absenceNotifications(enabled)
    }

    fun setAgendaNotifications(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.agendaNotifications(enabled)
    }

    fun setProjectNotifications(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.projectNotifications(enabled)
    }

    fun setDocumentNotifications(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.documentNotifications(enabled)
    }

    fun setRefreshInterval(feature: SyncFeature, minutes: Int) = launchSettingChange {
        updateSettingsUseCase.refreshInterval(feature, minutes)
        rescheduleSyncUseCase()
    }

    fun clearCache() = launchSettingChange {
        clearCacheUseCase()
    }

    fun logout() = launchSettingChange {
        logoutUseCase()
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
