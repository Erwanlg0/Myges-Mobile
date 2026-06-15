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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCalendarSyncAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarSyncPort {
    override suspend fun sync(events: List<AgendaEvent>) {
        withContext(Dispatchers.IO) {
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
    }

    private fun writableCalendarId(): Long? {
        val prefs = context.getSharedPreferences("calendar_settings", Context.MODE_PRIVATE)
        val savedId = prefs.getLong("selected_calendar_id", -1L)
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_TYPE
        )
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val args = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

        if (savedId != -1L) {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars._ID} = ? AND $selection",
                arrayOf(savedId.toString()) + args,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return savedId
                }
            }
        }

        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, args, null)?.use { cursor ->
            var fallbackId: Long? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val accountType = cursor.getString(1)
                if (accountType == "com.google") {
                    return id
                }
                if (fallbackId == null) {
                    fallbackId = id
                }
            }
            return fallbackId
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
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CUSTOM_APP_URI
        )
        return context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.CUSTOM_APP_PACKAGE} = ? OR ${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf(context.packageName, "$markerPrefix%"),
            null
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val description = cursor.getString(3).orEmpty()
                    val externalId = cursor.getString(8)
                        ?.removePrefix("myges://agenda/")
                        ?.takeIf { it.isNotBlank() }
                        ?: description.removePrefix(markerPrefix).substringBefore('\n').trim()
                    if (externalId.isBlank()) continue
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
        val loc = if (!room.isNullOrBlank() && !address.isNullOrBlank()) {
            if (address.contains(room, ignoreCase = true)) address
            else "$room - $address"
        } else {
            address ?: room
        }
        val descBuilder = StringBuilder()
        if (!teacher.isNullOrBlank()) {
            descBuilder.append("Intervenant : ").append(teacher).append("\n")
        }
        if (!room.isNullOrBlank() || !address.isNullOrBlank()) {
            descBuilder.append("Salle(s) :\n")
            val roomDetail = listOfNotNull(address, room).joinToString(" - ")
            descBuilder.append("- ").append(roomDetail).append("\n")
        }
        return DesiredCalendarEvent(
            externalId = id,
            calendarId = calendarId,
            title = title,
            description = descBuilder.toString().trim(),
            startsAtEpochMillis = startsAt.toEpochMilli(),
            endsAtEpochMillis = endsAt.toEpochMilli(),
            timeZone = timeZone,
            location = loc,
            colorId = colorId
        )
    }
}

private fun colorIdToArgb(colorId: String?): Int {
    return when (colorId) {
        "1" -> 0xFF7986CB.toInt() // Lavender
        "2" -> 0xFF33B679.toInt() // Sage
        "3" -> 0xFF8E24AA.toInt() // Grape
        "4" -> 0xFFE67C73.toInt() // Flamingo
        "5" -> 0xFFF6BF26.toInt() // Banana
        "6" -> 0xFFF4511E.toInt() // Tangerine
        "7" -> 0xFF039BE5.toInt() // Peacock
        "8" -> 0xFF616161.toInt() // Graphite
        "9" -> 0xFF3F51B5.toInt() // Basil
        "10" -> 0xFF0B8043.toInt() // Green
        else -> 0xFFFFF56F.toInt() // Default Yellow
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
    val location: String?,
    val colorId: String? = null
) {
    fun toContentValues(): ContentValues {
        return ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startsAtEpochMillis)
            put(CalendarContract.Events.DTEND, endsAtEpochMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
            put(CalendarContract.Events.EVENT_COLOR, colorIdToArgb(colorId))
            put(CalendarContract.Events.CUSTOM_APP_PACKAGE, "com.elg.myges")
            put(CalendarContract.Events.CUSTOM_APP_URI, "myges://agenda/$externalId")
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
