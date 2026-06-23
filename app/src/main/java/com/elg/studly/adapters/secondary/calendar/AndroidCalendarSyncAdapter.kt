package com.elg.studly.adapters.secondary.calendar

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.elg.studly.R
import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.CalendarAccount
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCalendarSyncAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarSyncPort {
    private companion object {
        const val BATCH_SIZE = 100
    }

    override suspend fun sync(events: List<AgendaEvent>) {
        withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.w("CalendarSync", "WRITE_CALENDAR not granted")
                    throw AppException(AppError.PermissionDenied)
                }
                val calendarId = writableCalendarId() ?: run {
                    android.util.Log.w("CalendarSync", "no writable calendar found")
                    throw AppException(AppError.Storage)
                }
                val resolver = context.contentResolver
                val zone = ZoneId.systemDefault()
                val timeZone = zone.id
                val eventColorKeys = eventColorKeysForCalendar(calendarId)
                val markerPrefix = context.getString(R.string.calendar_event_marker_prefix)
                val weekStart = LocalDate.now(zone).with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant()
                val desiredEvents = events
                    .filter { it.startsAt >= weekStart }
                    .map { event -> event.toCalendarEvent(calendarId, timeZone, eventColorKeys, markerPrefix) }
                val plan = calendarSyncPlan(currentEvents(calendarId, desiredEvents), desiredEvents)
                val operations = ArrayList<ContentProviderOperation>(plan.deletes.size + plan.updates.size + plan.inserts.size)
                plan.deletes.forEach { event ->
                    operations += ContentProviderOperation
                        .newDelete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.rowId))
                        .build()
                }
                plan.updates.forEach { update ->
                    operations += ContentProviderOperation
                        .newUpdate(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, update.current.rowId))
                        .withValues(update.desired.toContentValues())
                        .build()
                }
                plan.inserts.forEach { event ->
                    operations += ContentProviderOperation
                        .newInsert(CalendarContract.Events.CONTENT_URI)
                        .withValues(event.toContentValues())
                        .build()
                }
                operations.chunked(BATCH_SIZE).forEach { chunk ->
                    resolver.applyBatch(CalendarContract.AUTHORITY, ArrayList(chunk))
                }
            } catch (t: Throwable) {
                android.util.Log.e("CalendarSync", "sync failed", t)
                throw t
            }
        }
    }

    override suspend fun availableCalendars(): List<CalendarAccount> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val args = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, args, null)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CalendarAccount(
                            id = cursor.getLong(0),
                            displayName = cursor.getString(1).orEmpty(),
                            accountName = cursor.getString(2).orEmpty()
                        )
                    )
                }
            }
        }.orEmpty()
    }

    override suspend fun selectedCalendarId(): Long? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("calendar_settings", Context.MODE_PRIVATE)
        prefs.getLong("selected_calendar_id", -1L).takeIf { it != -1L }
    }

    override suspend fun selectCalendar(id: Long) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("calendar_settings", Context.MODE_PRIVATE)
        prefs.edit().putLong("selected_calendar_id", id).apply()
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

    
    private fun eventColorKeysForCalendar(calendarId: Long): Set<String> {
        val account = accountForCalendar(calendarId) ?: return emptySet()
        val (accountName, accountType) = account
        val projection = arrayOf(CalendarContract.Colors.COLOR_KEY)
        val selection = "${CalendarContract.Colors.ACCOUNT_NAME} = ? AND " +
            "${CalendarContract.Colors.ACCOUNT_TYPE} = ? AND " +
            "${CalendarContract.Colors.COLOR_TYPE} = ?"
        val args = arrayOf(accountName, accountType, CalendarContract.Colors.TYPE_EVENT.toString())
        return context.contentResolver.query(CalendarContract.Colors.CONTENT_URI, projection, selection, args, null)
            ?.use { cursor ->
                buildSet {
                    while (cursor.moveToNext()) {
                        cursor.getString(0)?.let { add(it) }
                    }
                }
            }.orEmpty()
    }

    private fun accountForCalendar(calendarId: Long): Pair<String, String>? {
        val projection = arrayOf(
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE
        )
        return context.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId),
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0) ?: return null
                val type = cursor.getString(1) ?: return null
                name to type
            } else null
        }
    }

    private fun currentEvents(calendarId: Long, desired: List<DesiredCalendarEvent>): List<CalendarEventRow> {
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
            CalendarContract.Events.CUSTOM_APP_URI,
            CalendarContract.Events.EVENT_COLOR_KEY
        )
        val selectionParts = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (desired.isNotEmpty()) {
            selectionParts += "(${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)"
            args += calendarId.toString()
            args += desired.minOf { it.startsAtEpochMillis }.toString()
            args += desired.maxOf { it.startsAtEpochMillis }.toString()
        }
        selectionParts += "${CalendarContract.Events.CUSTOM_APP_PACKAGE} = ?"
        args += context.packageName
        selectionParts += "${CalendarContract.Events.DESCRIPTION} LIKE ?"
        args += "%$markerPrefix%"
        return context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selectionParts.joinToString(" OR "),
            args.toTypedArray(),
            null
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val description = cursor.getString(3).orEmpty()
                    val externalId = cursor.getString(8)
                        ?.removePrefix("Studly://agenda/")
                        ?.takeIf { it.isNotBlank() }
                        ?: description.lineSequence()
                            .firstOrNull { it.startsWith(markerPrefix) }
                            ?.removePrefix(markerPrefix)
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                        ?: ""
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
                            location = cursor.getString(7),
                            colorKey = cursor.getString(9)
                        )
                    )
                }
            }
        }.orEmpty()
    }

    private fun AgendaEvent.toCalendarEvent(
        calendarId: Long,
        timeZone: String,
        eventColorKeys: Set<String>,
        markerPrefix: String
    ): DesiredCalendarEvent {
        val loc = address
        val descBuilder = StringBuilder()
        if (!teacher.isNullOrBlank()) {
            descBuilder.append("Intervenant : ").append(teacher).append("\n")
        }
        val roomList = room
            ?.split(',', ';', '/', '\n')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (roomList.isNotEmpty()) {
            if (roomList.size > 1) {
                descBuilder.append("Salles :\n")
                roomList.forEach { descBuilder.append("- ").append(it).append("\n") }
            } else {
                descBuilder.append("Salle : ").append(roomList.first()).append("\n")
            }
        }
        val colorKey = colorId?.takeIf { it in eventColorKeys }
        val body = descBuilder.toString().trim()
        val markerLine = "$markerPrefix$id"
        return DesiredCalendarEvent(
            externalId = id,
            calendarId = calendarId,
            title = title,
            description = if (body.isEmpty()) markerLine else "$body\n$markerLine",
            startsAtEpochMillis = startsAt.toEpochMilli(),
            endsAtEpochMillis = endsAt.toEpochMilli(),
            timeZone = timeZone,
            location = loc,
            colorKey = colorKey
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
    val location: String?,
    val colorKey: String? = null
) {
    fun sameContentAs(event: DesiredCalendarEvent): Boolean {
        return calendarId == event.calendarId &&
            title == event.title &&
            description == event.description &&
            startsAtEpochMillis == event.startsAtEpochMillis &&
            endsAtEpochMillis == event.endsAtEpochMillis &&
            timeZone == event.timeZone &&
            location == event.location &&
            colorKey == event.colorKey
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
    val colorKey: String? = null
) {
    fun toContentValues(): ContentValues {
        return ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startsAtEpochMillis)
            put(CalendarContract.Events.DTEND, endsAtEpochMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
            if (colorKey != null) {
                put(CalendarContract.Events.EVENT_COLOR_KEY, colorKey)
            }
            put(CalendarContract.Events.CUSTOM_APP_PACKAGE, "com.elg.studly")
            put(CalendarContract.Events.CUSTOM_APP_URI, "Studly://agenda/$externalId")
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
    val distinctDesired = desired.distinctBy(DesiredCalendarEvent::externalId)
    val byId = HashMap<String, ArrayList<CalendarEventRow>>()
    val bySignature = HashMap<String, ArrayList<CalendarEventRow>>()
    current.forEach { row ->
        if (row.externalId.isNotBlank()) byId.getOrPut(row.externalId) { ArrayList() }.add(row)
        bySignature.getOrPut(row.signature()) { ArrayList() }.add(row)
    }
    val consumed = HashSet<Long>()
    val inserts = ArrayList<DesiredCalendarEvent>()
    val updates = ArrayList<CalendarEventUpdate>()
    val deletes = ArrayList<CalendarEventRow>()

    distinctDesired.forEach { event ->
        val candidates = LinkedHashSet<CalendarEventRow>()
        byId[event.externalId]?.forEach { if (it.rowId !in consumed) candidates += it }
        bySignature[event.signature()]?.forEach { if (it.rowId !in consumed) candidates += it }
        if (candidates.isEmpty()) {
            inserts += event
            return@forEach
        }
        val iterator = candidates.iterator()
        val keep = iterator.next()
        consumed += keep.rowId
        if (!keep.sameContentAs(event)) updates += CalendarEventUpdate(keep, event)
        while (iterator.hasNext()) {
            val duplicate = iterator.next()
            consumed += duplicate.rowId
            deletes += duplicate
        }
    }
    current.forEach { row ->
        if (row.rowId !in consumed && row.externalId.isNotBlank()) deletes += row
    }

    return CalendarSyncPlan(inserts, updates, deletes)
}

private fun CalendarEventRow.signature(): String =
    "$calendarId|$title|$startsAtEpochMillis|$endsAtEpochMillis"

private fun DesiredCalendarEvent.signature(): String =
    "$calendarId|$title|$startsAtEpochMillis|$endsAtEpochMillis"
