package com.elg.studly.adapters.secondary.security

import android.content.Context
import android.content.SharedPreferences
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("secure_database", Context.MODE_PRIVATE)
    private val cipher = KeystoreBackedCipher(KEY_ALIAS, KeystoreBackedCipher.CURRENT_KEY_VERSION)

    fun readOrCreate(): ByteArray {
        return runCatching {
            preferences.getString(KEY_PASSPHRASE, null)
                ?.let(::decrypt)
                ?: createPassphrase()
        }.getOrElse {
            throw AppException(AppError.Storage)
        }
    }

    private fun createPassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES)
        SecureRandom().nextBytes(passphrase)
        preferences.edit()
            .putString(KEY_PASSPHRASE, encrypt(passphrase))
            .apply()
        return passphrase
    }

    private fun encrypt(value: ByteArray): String {
        return cipher.encrypt(value)
    }

    private fun decrypt(value: String): ByteArray {
        val result = cipher.decrypt(value)
        if (result.needsRotation) {
            preferences.edit()
                .putString(KEY_PASSPHRASE, encrypt(result.bytes))
                .apply()
        }
        return result.bytes
    }

    private companion object {
        const val KEY_ALIAS = "myges_database_key"
        const val KEY_PASSPHRASE = "passphrase"
        const val PASSPHRASE_BYTES = 32
    }
}
