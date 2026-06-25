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
    return readBytes().copyOf(SQLITE_HEADER.size).contentEquals(SQLITE_HEADER)
}

private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
