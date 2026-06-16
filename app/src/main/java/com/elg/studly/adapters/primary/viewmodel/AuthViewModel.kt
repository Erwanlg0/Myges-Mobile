package com.elg.studly.adapters.primary.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elg.studly.application.usecase.CompleteOAuthLoginUseCase
import com.elg.studly.application.usecase.ObserveLockedBiometricSessionUseCase
import com.elg.studly.application.usecase.UnlockWithBiometricsUseCase
import com.elg.studly.config.AppConfig
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean,
    val error: AppError?,
    val hasBiometricSession: Boolean,
    val authorizationUrl: String
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeLockedBiometricSession: ObserveLockedBiometricSessionUseCase,
    private val completeOAuthLoginUseCase: CompleteOAuthLoginUseCase,
    private val unlockWithBiometricsUseCase: UnlockWithBiometricsUseCase,
    appConfig: AppConfig
) : ViewModel() {
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<AppError?>(null)
    private val authorizationUrl = MutableStateFlow(appConfig.oauthAuthorizeUrl)

    val state: StateFlow<AuthUiState> = combine(
        loading,
        error,
        observeLockedBiometricSession(),
        authorizationUrl
    ) { isLoading, currentError, hasBiometricSession, oauthUrl ->
        AuthUiState(isLoading, currentError, hasBiometricSession, oauthUrl)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUiState(false, null, false, appConfig.oauthAuthorizeUrl)
    )

    fun completeOAuthCallback(uri: Uri) {
        val accessToken = uri.oauthParameter("access_token")
        if (accessToken.isNullOrBlank()) {
            error.value = AppError.Unauthorized
            return
        }
        val authorizationToken = accessToken.withAuthorizationScheme(uri.oauthParameter("token_type"))
        val expiresAt = uri.oauthParameter("expires_in")
            ?.toLongOrNull()
            ?.let { Instant.now().plusSeconds(it) }
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching {
                completeOAuthLoginUseCase(authorizationToken, expiresAt, false)
            }.onFailure {
                error.value = it.toAppError()
            }
            loading.value = false
        }
    }

    fun unlockWithBiometrics() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { unlockWithBiometricsUseCase() }
                .onFailure { error.value = it.toAppError() }
            loading.value = false
        }
    }

    private fun Uri.oauthParameter(name: String): String? {
        getQueryParameter(name)?.let { return it }
        val fragment = encodedFragment ?: return null
        return fragment.split('&')
            .firstOrNull { parameter -> parameter.substringBefore('=') == name }
            ?.substringAfter('=', "")
            ?.let { value -> URLDecoder.decode(value, StandardCharsets.UTF_8.name()) }
    }

    private fun String.withAuthorizationScheme(tokenType: String?): String {
        if (any(Char::isWhitespace) || tokenType.isNullOrBlank()) return this
        return "${tokenType.lowercase()} $this"
    }
}
