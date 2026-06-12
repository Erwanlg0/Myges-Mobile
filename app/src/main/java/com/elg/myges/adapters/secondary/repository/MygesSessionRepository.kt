package com.elg.myges.adapters.secondary.repository

import com.elg.myges.adapters.secondary.security.SecureSessionStore
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import com.elg.myges.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MygesSessionRepository @Inject constructor(
    private val secureSessionStore: SecureSessionStore
) : SessionRepository {
    private var storedSession: Session? = runCatching { secureSessionStore.read() }
        .getOrElse {
            secureSessionStore.clear()
            null
        }
    private val unlockedSession = MutableStateFlow(storedSession?.takeUnless { it.biometricEnabled })
    private val lockedBiometricSession = MutableStateFlow(storedSession?.biometricEnabled == true)

    override val session: StateFlow<Session?> = unlockedSession
    override val hasLockedBiometricSession: StateFlow<Boolean> = lockedBiometricSession

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        try {
            if (accessToken.isBlank()) throw AppException(AppError.Unauthorized)
            val session = Session(
                username = KORDIS_SESSION_USERNAME,
                accessToken = accessToken,
                refreshToken = null,
                expiresAt = expiresAt,
                biometricEnabled = enableBiometric
            )
            secureSessionStore.save(session)
            storedSession = session
            lockedBiometricSession.value = false
            unlockedSession.value = session
        } catch (throwable: Throwable) {
            throw throwable.toSessionAppException()
        }
    }

    override suspend fun unlockWithBiometrics() {
        val session = secureSessionStore.read() ?: throw AppException(AppError.Unauthorized)
        storedSession = session
        lockedBiometricSession.value = false
        unlockedSession.value = session
    }

    override suspend fun logout() {
        secureSessionStore.clear()
        storedSession = null
        lockedBiometricSession.value = false
        unlockedSession.value = null
    }

    private fun Throwable.toSessionAppException(): AppException {
        return when (this) {
            is AppException -> this
            else -> AppException(AppError.Unexpected(message))
        }
    }

    private companion object {
        const val KORDIS_SESSION_USERNAME = "Kordis"
    }
}
