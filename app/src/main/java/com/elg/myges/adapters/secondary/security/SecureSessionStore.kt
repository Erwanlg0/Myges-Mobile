package com.elg.myges.adapters.secondary.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import com.elg.myges.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)

    fun read(): Session? {
        return runCatching {
            val username = preferences.getString(KEY_USERNAME, null) ?: return null
            val encryptedToken = preferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
            val accessToken = decrypt(encryptedToken)
            val expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0L).takeIf { it > 0L }?.let(Instant::ofEpochMilli)
            val issuedAt = preferences.getLong(KEY_ISSUED_AT, 0L).takeIf { it > 0L }?.let(Instant::ofEpochMilli)
                ?: expiresAt?.minusSeconds(TOKEN_VALIDITY_SECONDS)
                ?: Instant.now()
            Session(
                username = username,
                accessToken = accessToken,
                refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)?.let(::decrypt),
                expiresAt = expiresAt,
                biometricEnabled = preferences.getBoolean(KEY_BIOMETRIC, false),
                issuedAt = issuedAt,
                refreshAfter = preferences.getLong(KEY_REFRESH_AFTER, 0L).takeIf { it > 0L }?.let(Instant::ofEpochMilli)
                    ?: issuedAt.plusSeconds(TOKEN_REFRESH_SECONDS)
            )
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
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        return "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(cipherText, Base64.NO_WRAP)}"
    }

    private fun decrypt(value: String): String {
        val parts = value.split(':')
        if (parts.size != 2) throw AppException(AppError.Storage)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_ALIAS = "myges_session_key"
        const val KEY_BIOMETRIC = "biometric_enabled"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_ISSUED_AT = "issued_at"
        const val KEY_REFRESH_AFTER = "refresh_after"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USERNAME = "username"
        const val TOKEN_REFRESH_SECONDS = 5L * 24L * 60L * 60L
        const val TOKEN_VALIDITY_SECONDS = 7L * 24L * 60L * 60L
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
