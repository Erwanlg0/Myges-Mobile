package com.elg.studly.adapters.secondary.calendar

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSyncPlanTest {
    @Test
    fun identicalEventsProduceNoOperations() {
        val current = calendarRow("course-1", title = "Math")
        val desired = desiredEvent("course-1", title = "Math")

        val plan = calendarSyncPlan(listOf(current), listOf(desired))

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(emptyList<CalendarEventUpdate>(), plan.updates)
        assertEquals(emptyList<CalendarEventRow>(), plan.deletes)
    }

    @Test
    fun missingCurrentEventIsInserted() {
        val desired = desiredEvent("course-1")

        val plan = calendarSyncPlan(emptyList(), listOf(desired))

        assertEquals(listOf(desired), plan.inserts)
        assertEquals(emptyList<CalendarEventUpdate>(), plan.updates)
        assertEquals(emptyList<CalendarEventRow>(), plan.deletes)
    }

    @Test
    fun changedCurrentEventIsUpdated() {
        val current = calendarRow("course-1", title = "Old")
        val desired = desiredEvent("course-1", title = "New")

        val plan = calendarSyncPlan(listOf(current), listOf(desired))

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(listOf(CalendarEventUpdate(current, desired)), plan.updates)
        assertEquals(emptyList<CalendarEventRow>(), plan.deletes)
    }

    @Test
    fun staleCurrentEventIsDeleted() {
        val current = calendarRow("course-1")

        val plan = calendarSyncPlan(listOf(current), emptyList())

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(emptyList<CalendarEventUpdate>(), plan.updates)
        assertEquals(listOf(current), plan.deletes)
    }

    @Test
    fun untaggedOrphanMatchedBySignatureIsReusedNotDuplicated() {
        val orphan = calendarRow("", title = "Math", rowId = 7L).copy(description = "Salle : A101")
        val desired = desiredEvent("course-1", title = "Math")

        val plan = calendarSyncPlan(listOf(orphan), listOf(desired))

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(listOf(CalendarEventUpdate(orphan, desired)), plan.updates)
        assertEquals(emptyList<CalendarEventRow>(), plan.deletes)
    }

    @Test
    fun stackedDuplicatesCollapseToOneKeepingExtrasDeleted() {
        val first = calendarRow("course-1", title = "Math", rowId = 1L)
        val dupeTagged = calendarRow("course-1", title = "Math", rowId = 2L)
        val dupeOrphan = calendarRow("", title = "Math", rowId = 3L)
        val desired = desiredEvent("course-1", title = "Math")

        val plan = calendarSyncPlan(listOf(first, dupeTagged, dupeOrphan), listOf(desired))

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(emptyList<CalendarEventUpdate>(), plan.updates)
        assertEquals(listOf(dupeTagged, dupeOrphan), plan.deletes)
    }

    @Test
    fun untaggedUserEventIsLeftUntouched() {
        val userEvent = calendarRow("", title = "Dentist", rowId = 9L)

        val plan = calendarSyncPlan(listOf(userEvent), emptyList())

        assertEquals(emptyList<DesiredCalendarEvent>(), plan.inserts)
        assertEquals(emptyList<CalendarEventUpdate>(), plan.updates)
        assertEquals(emptyList<CalendarEventRow>(), plan.deletes)
    }

    private fun calendarRow(
        id: String,
        title: String = "Course",
        rowId: Long = 42L
    ): CalendarEventRow {
        return CalendarEventRow(
            rowId = rowId,
            externalId = id,
            calendarId = 1L,
            title = title,
            description = "MyGES:$id",
            startsAtEpochMillis = 1_000L,
            endsAtEpochMillis = 2_000L,
            timeZone = "Europe/Paris",
            location = "A101"
        )
    }

    private fun desiredEvent(
        id: String,
        title: String = "Course"
    ): DesiredCalendarEvent {
        return DesiredCalendarEvent(
            externalId = id,
            calendarId = 1L,
            title = title,
            description = "MyGES:$id",
            startsAtEpochMillis = 1_000L,
            endsAtEpochMillis = 2_000L,
            timeZone = "Europe/Paris",
            location = "A101"
        )
    }
}
