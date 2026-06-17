package com.elg.studly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DomainModelsTest {

    @Test
    fun testRefreshIntervalsMinutesFor() {
        val intervals = RefreshIntervals(
            agenda = 20,
            grades = 30,
            absences = 40,
            projects = 50,
            documents = 60,
            directory = 70,
            news = 80
        )
        
        assertEquals(20, intervals.minutesFor(SyncFeature.Agenda))
        assertEquals(30, intervals.minutesFor(SyncFeature.Grades))
        assertEquals(40, intervals.minutesFor(SyncFeature.Absences))
        assertEquals(50, intervals.minutesFor(SyncFeature.Projects))
        assertEquals(60, intervals.minutesFor(SyncFeature.Documents))
        assertEquals(70, intervals.minutesFor(SyncFeature.Directory))
        assertEquals(80, intervals.minutesFor(SyncFeature.News))
    }

    @Test
    fun testRefreshIntervalsWith() {
        val intervals = RefreshIntervals()
        
        val updated = intervals.with(SyncFeature.Agenda, 45)
        assertEquals(45, updated.minutesFor(SyncFeature.Agenda))
        assertEquals(DEFAULT_REFRESH_MINUTES, updated.minutesFor(SyncFeature.Grades))
        
        // Test clamping (minimum is 15, maximum is 1440)
        val clampedMin = intervals.with(SyncFeature.Grades, 5)
        assertEquals(MIN_REFRESH_MINUTES, clampedMin.minutesFor(SyncFeature.Grades))
        
        val clampedMax = intervals.with(SyncFeature.Grades, 2000)
        assertEquals(MAX_REFRESH_MINUTES, clampedMax.minutesFor(SyncFeature.Grades))
    }

    @Test
    fun testRefreshIntervalsSmallestIntervalMinutes() {
        val intervals = RefreshIntervals(
            agenda = 60,
            grades = 45,
            absences = 120
        )
        
        assertEquals(45, intervals.smallestIntervalMinutes())
    }

    @Test
    fun testClampRefreshMinutes() {
        assertEquals(15, clampRefreshMinutes(5))
        assertEquals(30, clampRefreshMinutes(30))
        assertEquals(1440, clampRefreshMinutes(2000))
    }

    @Test
    fun testClampReminderLeadMinutes() {
        assertEquals(0, clampReminderLeadMinutes(-5))
        assertEquals(15, clampReminderLeadMinutes(15))
        assertEquals(0, clampReminderLeadMinutes(14))
        assertEquals(60, clampReminderLeadMinutes(60))
        assertEquals(1440, clampReminderLeadMinutes(1440))
        assertEquals(0, clampReminderLeadMinutes(2000))
    }

    @Test
    fun testUserSettingsLeadFor() {
        val prefs = NotificationPreferences(true, true, true, true, true)
        val settings = UserSettings(
            languageTag = null,
            notifications = prefs,
            calendarSyncEnabled = false,
            classReminderLeadMinutes = 15,
            deadlineReminderLeadMinutes = 60,
            lastSyncAt = null
        )
        
        assertEquals(15, settings.leadFor(ReminderKind.Class))
        assertEquals(60, settings.leadFor(ReminderKind.Deadline))
    }
}
