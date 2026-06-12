package com.elg.myges.adapters.primary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.myges.application.usecase.ClearCacheUseCase
import com.elg.myges.application.usecase.LogoutUseCase
import com.elg.myges.application.usecase.ObserveSettingsUseCase
import com.elg.myges.application.usecase.UpdateSettingsUseCase
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.UserSettings
import com.elg.myges.domain.model.toAppError
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
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)

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

    fun setCalendarSync(enabled: Boolean) = launchSettingChange {
        updateSettingsUseCase.calendarSync(enabled)
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
