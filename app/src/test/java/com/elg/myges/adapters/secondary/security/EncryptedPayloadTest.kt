package com.elg.myges.adapters.secondary.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class EncryptedPayloadTest {
    @Test
    fun versionedPayloadRoundTripsVersionAndBytes() {
        val iv = byteArrayOf(1, 2, 3)
        val cipherText = byteArrayOf(4, 5, 6)
        val encoded = EncryptedPayload.encode(2, iv, cipherText)

        val decoded = EncryptedPayload.decode(encoded)

        assertEquals(2, decoded.version)
        assertArrayEquals(iv, decoded.iv)
        assertArrayEquals(cipherText, decoded.cipherText)
    }

    @Test
    fun legacyPayloadWithoutVersionStaysReadable() {
        val iv = byteArrayOf(7, 8, 9)
        val cipherText = byteArrayOf(10, 11, 12)
        val encoded = "${iv.toBase64()}:${cipherText.toBase64()}"

        val decoded = EncryptedPayload.decode(encoded)

        assertNull(decoded.version)
        assertArrayEquals(iv, decoded.iv)
        assertArrayEquals(cipherText, decoded.cipherText)
    }

    private fun ByteArray.toBase64(): String {
        return Base64.getEncoder().encodeToString(this)
    }
}
