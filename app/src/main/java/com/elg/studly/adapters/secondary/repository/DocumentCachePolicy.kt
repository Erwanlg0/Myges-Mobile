package com.elg.studly.adapters.secondary.repository

import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

internal fun purgeExpiredDocumentCache(
    directory: File,
    now: Instant = kotlin.time.Clock.System.now(),
    maxAge: Duration = 30.days
) {
    if (!directory.exists()) return
    val expiresBefore = now.minus(maxAge).toEpochMilli()
    directory.walkBottomUp()
        .filter { file -> file != directory }
        .forEach { file ->
            if (file.isFile && file.lastModified() < expiresBefore) {
                file.delete()
            }
            if (file.isDirectory && file.listFiles().isEmpty()) {
                file.delete()
            }
        }
}
