package com.elg.studly.adapters.primary.state

import androidx.annotation.StringRes
import com.elg.studly.R
import com.elg.studly.domain.model.AppError

data class FeatureUiState<T>(
    val data: T,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: AppError? = null,
    val online: Boolean = true
)

@StringRes
fun AppError.messageRes(): Int {
    return when (this) {
        AppError.EmptyResponse -> R.string.error_empty_response
        AppError.Network -> R.string.error_network
        AppError.Offline -> R.string.error_offline
        AppError.PermissionDenied -> R.string.error_permission_denied
        is AppError.Remote -> R.string.error_remote
        AppError.Storage -> R.string.error_storage
        AppError.DocumentUnavailable -> R.string.error_document_unavailable
        AppError.Unauthorized -> R.string.error_unauthorized
        AppError.LoginFailed -> R.string.error_login_failed
        is AppError.Unexpected -> R.string.error_unexpected
    }
}
