package com.elg.myges.adapters.secondary.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.elg.myges.R
import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCalendarSyncAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarSyncPort {
    override suspend fun sync(events: List<AgendaEvent>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            throw AppException(AppError.PermissionDenied)
        }
        val calendarId = writableCalendarId() ?: throw AppException(AppError.Storage)
        val resolver = context.contentResolver
        resolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf("${context.getString(R.string.calendar_event_marker_prefix)}%")
        )
        events.forEach { event ->
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.calendar_event_description, event.id))
                put(CalendarContract.Events.DTSTART, event.startsAt.toEpochMilli())
                put(CalendarContract.Events.DTEND, event.endsAt.toEpochMilli())
                put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().id)
                event.room?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
            }
            resolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }
    }

    private fun writableCalendarId(): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val args = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, args, null)?.use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
        return null
    }
}
