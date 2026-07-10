package com.elg.studly.adapters.secondary.storage

import android.content.Context
import java.io.File

fun deletePlaintextDatabaseIfPresent(context: Context, databaseName: String) {
    val databaseFile = context.getDatabasePath(databaseName)
    if (!databaseFile.exists() || !databaseFile.hasPlaintextSqliteHeader()) return
    context.deleteDatabase(databaseName)
}

internal fun File.hasPlaintextSqliteHeader(): Boolean {
    if (length() < SQLITE_HEADER.size) return false
    val header = ByteArray(SQLITE_HEADER.size)
    inputStream().use { input ->
        var offset = 0
        while (offset < header.size) {
            val read = input.read(header, offset, header.size - offset)
            if (read < 0) return false
            offset += read
        }
    }
    return header.contentEquals(SQLITE_HEADER)
}

private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
