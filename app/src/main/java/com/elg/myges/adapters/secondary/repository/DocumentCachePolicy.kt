package com.elg.myges.adapters.secondary.repository

import java.io.File
import java.time.Duration
import java.time.Instant

internal fun purgeExpiredDocumentCache(
    directory: File,
    now: Instant = Instant.now(),
    maxAge: Duration = Duration.ofDays(30)
) {
    if (!directory.exists()) return
    val expiresBefore = now.minus(maxAge).toEpochMilli()
    directory.walkBottomUp()
        .filter { file -> file != directory }
        .forEach { file ->
            if (file.isFile && file.lastModified() < expiresBefore) {
                file.delete()
            }
            if (file.isDirectory && file.list()?.isEmpty() == true) {
                file.delete()
            }
        }
}
