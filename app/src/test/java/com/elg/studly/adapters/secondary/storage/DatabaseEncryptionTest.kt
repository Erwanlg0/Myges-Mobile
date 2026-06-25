package com.elg.studly.adapters.secondary.storage

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DatabaseEncryptionTest {
    @Test
    fun plaintextDatabaseIsDeleted() {
        val file = temporaryFile("SQLite format 3\u0000cached-data".toByteArray(Charsets.US_ASCII))
        val context = mockk<Context>()
        every { context.getDatabasePath("Studly.db") } returns file
        every { context.deleteDatabase("Studly.db") } returns true

        deletePlaintextDatabaseIfPresent(context, "Studly.db")

        verify { context.deleteDatabase("Studly.db") }
    }

    @Test
    fun missingDatabaseIsIgnored() {
        val file = File(createTempDir(), "missing.db")
        val context = mockk<Context>()
        every { context.getDatabasePath("Studly.db") } returns file

        deletePlaintextDatabaseIfPresent(context, "Studly.db")

        verify(exactly = 0) { context.deleteDatabase(any()) }
    }

    @Test
    fun encryptedDatabaseIsIgnored() {
        val file = temporaryFile(byteArrayOf(12, 42, 99, 3, 8, 71, 11, 90, 1, 5, 9, 2, 17, 31, 44, 61))
        val context = mockk<Context>()
        every { context.getDatabasePath("Studly.db") } returns file

        deletePlaintextDatabaseIfPresent(context, "Studly.db")

        verify(exactly = 0) { context.deleteDatabase(any()) }
    }

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

    @Test
    fun shortHeaderIsNotDetectedAsPlaintext() {
        val file = temporaryFile("SQLite".toByteArray(Charsets.US_ASCII))

        assertFalse(file.hasPlaintextSqliteHeader())
    }

    private fun temporaryFile(bytes: ByteArray): File {
        return File.createTempFile("Studly-db", ".db").apply {
            writeBytes(bytes)
            deleteOnExit()
        }
    }
}
