package com.elg.studly.adapters.secondary.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreBackedCipher(
    private val keyAlias: String,
    private val keyVersion: Int
) {
    fun encrypt(value: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(versionedAlias(keyVersion)))
        val cipherText = cipher.doFinal(value)
        return EncryptedPayload.encode(keyVersion, cipher.iv, cipherText)
    }

    fun decrypt(value: String): DecryptionResult {
        val payload = EncryptedPayload.decode(value)
        val alias = payload.version?.let(::versionedAlias) ?: keyAlias
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(alias), GCMParameterSpec(128, payload.iv))
        return DecryptionResult(
            bytes = cipher.doFinal(payload.cipherText),
            needsRotation = payload.version != keyVersion
        )
    }

    private fun secretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private fun versionedAlias(version: Int): String {
        return "${keyAlias}_v$version"
    }

    data class DecryptionResult(
        val bytes: ByteArray,
        val needsRotation: Boolean
    )

    companion object {
        const val CURRENT_KEY_VERSION = 2
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

internal data class EncryptedPayload(
    val version: Int?,
    val iv: ByteArray,
    val cipherText: ByteArray
) {
    companion object {
        fun encode(version: Int, iv: ByteArray, cipherText: ByteArray): String {
            return "v$version:${iv.toBase64()}:${cipherText.toBase64()}"
        }

        fun decode(value: String): EncryptedPayload {
            val parts = value.split(':')
            if (parts.size == 2) {
                return EncryptedPayload(
                    version = null,
                    iv = Base64.getDecoder().decode(parts[0]),
                    cipherText = Base64.getDecoder().decode(parts[1])
                )
            }
            if (parts.size == 3 && parts[0].startsWith('v')) {
                return EncryptedPayload(
                    version = parts[0].drop(1).toIntOrNull() ?: throw AppException(AppError.Storage),
                    iv = Base64.getDecoder().decode(parts[1]),
                    cipherText = Base64.getDecoder().decode(parts[2])
                )
            }
            throw AppException(AppError.Storage)
        }

        private fun ByteArray.toBase64(): String {
            return Base64.getEncoder().encodeToString(this)
        }
    }
}
