package com.elg.myges.adapters.secondary.repository

import java.io.File
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentCachePolicyTest {
    @Test
    fun purgeExpiredDocumentCacheDeletesFilesOlderThanMaxAge() {
        val directory = createTempDir()
        val now = Instant.parse("2026-06-12T12:00:00Z")
        val expired = File(directory, "expired.pdf").apply {
            writeText("expired")
            setLastModified(now.minus(Duration.ofDays(31)).toEpochMilli())
        }
        val current = File(directory, "current.pdf").apply {
            writeText("current")
            setLastModified(now.minus(Duration.ofDays(29)).toEpochMilli())
        }

        purgeExpiredDocumentCache(directory, now, Duration.ofDays(30))

        assertFalse(expired.exists())
        assertTrue(current.exists())
    }

    @Test
    fun purgeExpiredDocumentCacheRemovesEmptyNestedDirectories() {
        val directory = createTempDir()
        val nested = File(directory, "nested").apply { mkdirs() }
        File(nested, "expired.pdf").apply {
            writeText("expired")
            setLastModified(Instant.parse("2026-05-01T12:00:00Z").toEpochMilli())
        }

        purgeExpiredDocumentCache(
            directory = directory,
            now = Instant.parse("2026-06-12T12:00:00Z"),
            maxAge = Duration.ofDays(30)
        )

        assertFalse(nested.exists())
    }
}
