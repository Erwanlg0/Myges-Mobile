package com.elg.studly.adapters.secondary.repository

import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
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
            setLastModified(now.minus(31.days).toEpochMilli())
        }
        val current = File(directory, "current.pdf").apply {
            writeText("current")
            setLastModified(now.minus(29.days).toEpochMilli())
        }

        purgeExpiredDocumentCache(directory, now, 30.days)

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
            maxAge = 30.days
        )

        assertFalse(nested.exists())
    }

    @Test
    fun purgeExpiredDocumentCacheIgnoresMissingDirectory() {
        val directory = File(createTempDir(), "missing")

        purgeExpiredDocumentCache(directory)

        assertFalse(directory.exists())
    }

    @Test
    fun purgeExpiredDocumentCacheKeepsNonEmptyDirectories() {
        val directory = createTempDir()
        val nested = File(directory, "nested").apply { mkdirs() }
        File(nested, "current.pdf").apply {
            writeText("current")
            setLastModified(Instant.parse("2026-06-11T12:00:00Z").toEpochMilli())
        }

        purgeExpiredDocumentCache(
            directory = directory,
            now = Instant.parse("2026-06-12T12:00:00Z"),
            maxAge = 30.days
        )

        assertTrue(nested.exists())
    }
}
