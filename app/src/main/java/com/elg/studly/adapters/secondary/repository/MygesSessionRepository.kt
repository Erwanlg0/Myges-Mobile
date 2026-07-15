package com.elg.studly.adapters.secondary.repository

import com.elg.studly.adapters.secondary.security.SecureSessionStore
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MygesSessionRepository @Inject constructor(
    private val secureSessionStore: SecureSessionStore
) : SessionRepository {
    @Volatile private var storedSession: Session? = readUsableSession()
    private val unlockedSession = MutableStateFlow(storedSession?.takeUnless { it.biometricEnabled })
    private val lockedBiometricSession = MutableStateFlow(storedSession?.biometricEnabled == true)

    override val session: StateFlow<Session?> = unlockedSession
    override val hasLockedBiometricSession: StateFlow<Boolean> = lockedBiometricSession

    override fun currentSession(): Session? = storedSession

    @Synchronized
    override fun invalidateSession() {
        secureSessionStore.clear()
        storedSession = null
        lockedBiometricSession.value = false
        unlockedSession.value = null
    }

    @Synchronized
    override fun invalidateSessionIfCurrent(session: Session) {
        if (storedSession == session) invalidateSession()
    }

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        try {
            if (accessToken.isBlank()) throw AppException(AppError.Unauthorized)
            val issuedAt = Instant.now()
            val resolvedExpiresAt = expiresAt ?: issuedAt.plusSeconds(TOKEN_VALIDITY_SECONDS)
            val session = Session(
                username = KORDIS_SESSION_USERNAME,
                accessToken = accessToken,
                expiresAt = resolvedExpiresAt,
                biometricEnabled = enableBiometric,
                issuedAt = issuedAt,
                refreshAfter = minOf(issuedAt.plusSeconds(TOKEN_REFRESH_SECONDS), resolvedExpiresAt)
            )
            secureSessionStore.save(session)
            storedSession = session
            lockedBiometricSession.value = false
            unlockedSession.value = session
        } catch (throwable: Throwable) {
            throw throwable.toSessionAppException()
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        synchronized(this) {
            val session = storedSession ?: return
            val updated = session.copy(biometricEnabled = enabled)
            secureSessionStore.save(updated)
            storedSession = updated
            if (!enabled) lockedBiometricSession.value = false
            if (unlockedSession.value != null || !enabled) unlockedSession.value = updated
        }
    }

    override suspend fun unlockWithBiometrics() {
        val session = secureSessionStore.read() ?: throw AppException(AppError.Unauthorized)
        if (session.isExpired || session.requiresRefresh) {
            invalidateSession()
            throw AppException(AppError.Unauthorized)
        }
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

    private fun readUsableSession(): Session? {
        return runCatching { secureSessionStore.read() }
            .getOrElse {
                secureSessionStore.clear()
                null
            }
            ?.takeUnless { session ->
                (session.isExpired || session.requiresRefresh).also { shouldInvalidate ->
                    if (shouldInvalidate) secureSessionStore.clear()
                }
            }
    }

    private companion object {
        const val KORDIS_SESSION_USERNAME = "Kordis"
        const val TOKEN_REFRESH_SECONDS = 5L * 24L * 60L * 60L
        const val TOKEN_VALIDITY_SECONDS = 7L * 24L * 60L * 60L
    }
}
