package com.elg.myges.adapters.secondary.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DatabaseEncryptionTest {
    @Test
    fun plaintextSqliteHeaderIsDetected() {
        val file = temporaryFile("SQLite format 3\u0000cached-data".toByteArray(Charsets.US_ASCII))

        assertTrue(file.hasPlaintextSqliteHeader())
    }

    @Test
    fun encryptedDatabaseHeaderIsNotDetectedAsPlaintext() {
        val file = temporaryFile(byteArrayOf(12, 42, 99, 3, 8, 71, 11, 90, 1, 5, 9, 2, 17, 31, 44, 61))

        assertFalse(file.hasPlaintextSqliteHeader())
    }

    private fun temporaryFile(bytes: ByteArray): File {
        return File.createTempFile("myges-db", ".db").apply {
            writeBytes(bytes)
            deleteOnExit()
        }
    }
}
