package com.elg.myges.adapters.primary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.myges.application.usecase.ObserveSessionUseCase
import com.elg.myges.application.usecase.ObserveSettingsUseCase
import com.elg.myges.domain.model.Session
import com.elg.myges.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    observeSettings: ObserveSettingsUseCase
) : ViewModel() {
    val session: StateFlow<Session?> = observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val settings: StateFlow<UserSettings?> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
