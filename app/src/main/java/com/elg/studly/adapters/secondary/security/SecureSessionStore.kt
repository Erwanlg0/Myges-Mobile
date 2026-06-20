package com.elg.studly.adapters.secondary.security

import android.content.Context
import android.content.SharedPreferences
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    private val cipher = KeystoreBackedCipher(KEY_ALIAS, KeystoreBackedCipher.CURRENT_KEY_VERSION)

    fun read(): Session? {
        return runCatching {
            val username = preferences.getString(KEY_USERNAME, null) ?: return null
            val encryptedToken = preferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
            val accessToken = decrypt(encryptedToken)
            val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)?.let(::decrypt)
            val expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0L).takeIf { it > 0L }?.let(Instant::fromEpochMilliseconds)
            val issuedAt = preferences.getLong(KEY_ISSUED_AT, 0L).takeIf { it > 0L }?.let(Instant::fromEpochMilliseconds)
                ?: expiresAt?.minusSeconds(TOKEN_VALIDITY_SECONDS)
                ?: kotlin.time.Clock.System.now()
            val session = Session(
                username = username,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                biometricEnabled = preferences.getBoolean(KEY_BIOMETRIC, false),
                issuedAt = issuedAt,
                refreshAfter = preferences.getLong(KEY_REFRESH_AFTER, 0L).takeIf { it > 0L }?.let(Instant::fromEpochMilliseconds)
                    ?: issuedAt.plusSeconds(TOKEN_REFRESH_SECONDS)
            )
            if (preferences.getBoolean(KEY_NEEDS_ROTATION, false)) {
                save(session)
                preferences.edit().remove(KEY_NEEDS_ROTATION).apply()
            }
            session
        }.getOrElse {
            throw AppException(AppError.Storage)
        }
    }

    fun save(session: Session) {
        runCatching {
            preferences.edit()
                .putString(KEY_USERNAME, session.username)
                .putString(KEY_ACCESS_TOKEN, encrypt(session.accessToken))
                .putString(KEY_REFRESH_TOKEN, session.refreshToken?.let(::encrypt))
                .putLong(KEY_EXPIRES_AT, session.expiresAt?.toEpochMilli() ?: 0L)
                .putBoolean(KEY_BIOMETRIC, session.biometricEnabled)
                .putLong(KEY_ISSUED_AT, session.issuedAt.toEpochMilli())
                .putLong(KEY_REFRESH_AFTER, session.refreshAfter.toEpochMilli())
                .apply()
        }.getOrElse {
            throw AppException(AppError.Storage)
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun encrypt(value: String): String {
        return cipher.encrypt(value.toByteArray(Charsets.UTF_8))
    }

    private fun decrypt(value: String): String {
        val result = cipher.decrypt(value)
        if (result.needsRotation) preferences.edit().putBoolean(KEY_NEEDS_ROTATION, true).apply()
        return String(result.bytes, Charsets.UTF_8)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_ALIAS = "myges_session_key"
        const val KEY_BIOMETRIC = "biometric_enabled"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_ISSUED_AT = "issued_at"
        const val KEY_NEEDS_ROTATION = "needs_rotation"
        const val KEY_REFRESH_AFTER = "refresh_after"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USERNAME = "username"
        const val TOKEN_REFRESH_SECONDS = 5L * 24L * 60L * 60L
        const val TOKEN_VALIDITY_SECONDS = 7L * 24L * 60L * 60L
    }
}
