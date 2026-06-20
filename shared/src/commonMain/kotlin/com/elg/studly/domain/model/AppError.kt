package com.elg.studly.domain.model

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object Offline : AppError
    data object EmptyResponse : AppError
    data object PermissionDenied : AppError
    data object Storage : AppError
    data object DocumentUnavailable : AppError
    data class Remote(val code: Int?, val message: String?) : AppError
    data class Unexpected(val message: String?) : AppError
}

data class AppException(val error: AppError) : Exception(error.toString())

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppException -> error
        else -> AppError.Unexpected(message)
    }
}
