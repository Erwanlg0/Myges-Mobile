package com.elg.myges.adapters.secondary.calendar

import android.Manifest
import android.content.ContentUris
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
        val timeZone = ZoneId.systemDefault().id
        val desiredEvents = events.map { event -> event.toCalendarEvent(calendarId, timeZone) }
        val plan = calendarSyncPlan(currentEvents(), desiredEvents)
        plan.deletes.forEach { event ->
            resolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.rowId), null, null)
        }
        plan.updates.forEach { update ->
            resolver.update(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, update.current.rowId),
                update.desired.toContentValues(),
                null,
                null
            )
        }
        plan.inserts.forEach { event ->
            resolver.insert(CalendarContract.Events.CONTENT_URI, event.toContentValues())
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

    private fun currentEvents(): List<CalendarEventRow> {
        val markerPrefix = context.getString(R.string.calendar_event_marker_prefix)
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.EVENT_LOCATION
        )
        return context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf("$markerPrefix%"),
            null
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val description = cursor.getString(3) ?: continue
                    val externalId = description.removePrefix(markerPrefix).takeIf { it.isNotBlank() } ?: continue
                    add(
                        CalendarEventRow(
                            rowId = cursor.getLong(0),
                            externalId = externalId,
                            calendarId = cursor.getLong(1),
                            title = cursor.getString(2).orEmpty(),
                            description = description,
                            startsAtEpochMillis = cursor.getLong(4),
                            endsAtEpochMillis = cursor.getLong(5),
                            timeZone = cursor.getString(6).orEmpty(),
                            location = cursor.getString(7)
                        )
                    )
                }
            }
        }.orEmpty()
    }

    private fun AgendaEvent.toCalendarEvent(calendarId: Long, timeZone: String): DesiredCalendarEvent {
        return DesiredCalendarEvent(
            externalId = id,
            calendarId = calendarId,
            title = title,
            description = context.getString(R.string.calendar_event_description, id),
            startsAtEpochMillis = startsAt.toEpochMilli(),
            endsAtEpochMillis = endsAt.toEpochMilli(),
            timeZone = timeZone,
            location = room
        )
    }
}

internal data class CalendarEventRow(
    val rowId: Long,
    val externalId: String,
    val calendarId: Long,
    val title: String,
    val description: String,
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long,
    val timeZone: String,
    val location: String?
) {
    fun sameContentAs(event: DesiredCalendarEvent): Boolean {
        return calendarId == event.calendarId &&
            title == event.title &&
            description == event.description &&
            startsAtEpochMillis == event.startsAtEpochMillis &&
            endsAtEpochMillis == event.endsAtEpochMillis &&
            timeZone == event.timeZone &&
            location == event.location
    }
}

internal data class DesiredCalendarEvent(
    val externalId: String,
    val calendarId: Long,
    val title: String,
    val description: String,
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long,
    val timeZone: String,
    val location: String?
) {
    fun toContentValues(): ContentValues {
        return ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startsAtEpochMillis)
            put(CalendarContract.Events.DTEND, endsAtEpochMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
            if (location == null) {
                putNull(CalendarContract.Events.EVENT_LOCATION)
            } else {
                put(CalendarContract.Events.EVENT_LOCATION, location)
            }
        }
    }
}

internal data class CalendarSyncPlan(
    val inserts: List<DesiredCalendarEvent>,
    val updates: List<CalendarEventUpdate>,
    val deletes: List<CalendarEventRow>
)

internal data class CalendarEventUpdate(
    val current: CalendarEventRow,
    val desired: DesiredCalendarEvent
)

internal fun calendarSyncPlan(
    current: List<CalendarEventRow>,
    desired: List<DesiredCalendarEvent>
): CalendarSyncPlan {
    val currentById = current.associateBy(CalendarEventRow::externalId)
    val distinctDesired = desired.distinctBy(DesiredCalendarEvent::externalId)
    val desiredById = distinctDesired.associateBy(DesiredCalendarEvent::externalId)
    return CalendarSyncPlan(
        inserts = distinctDesired.filter { it.externalId !in currentById },
        updates = distinctDesired.mapNotNull { event ->
            val currentEvent = currentById[event.externalId] ?: return@mapNotNull null
            if (currentEvent.sameContentAs(event)) null else CalendarEventUpdate(currentEvent, event)
        },
        deletes = current.filter { it.externalId !in desiredById }
    )
}
