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
        assertEquals(DEFAULT_REFRESH_MINUTES, intervals.minutesFor(SyncFeature.Events))
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

        val events = intervals.with(SyncFeature.Events, 90)
        assertEquals(90, events.minutesFor(SyncFeature.Events))
        assertEquals(25, intervals.with(SyncFeature.Absences, 25).minutesFor(SyncFeature.Absences))
        assertEquals(35, intervals.with(SyncFeature.Projects, 35).minutesFor(SyncFeature.Projects))
        assertEquals(55, intervals.with(SyncFeature.Documents, 55).minutesFor(SyncFeature.Documents))
        assertEquals(65, intervals.with(SyncFeature.Directory, 65).minutesFor(SyncFeature.Directory))
        assertEquals(75, intervals.with(SyncFeature.News, 75).minutesFor(SyncFeature.News))
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
        assertEquals(listOf(0, 15, 30, 60, 120, 1440), REMINDER_LEAD_CHOICES)
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

    @Test
    fun testDomainModelDefaults() {
        val now = Instant.parse("2026-06-12T08:00:00Z")
        val news = NewsItem("news-1", "News", "Body", now)
        val event = StudentEvent("event-1", "Event", null, null, null, null, now, null, null, false)
        val dashboard = DashboardSummary(null, null, emptyList(), emptyList(), emptyList(), now)
        val message = ProjectMessage("message-1", "Alice", "Hello", now, true)
        val reminder = ReminderTarget("reminder-1", "Deadline", now, ReminderKind.Deadline, "projects")
        val account = CalendarAccount(1L, "Calendar", "account@example.com")
        val session = Session("user", "token", null, null, false, now, Instant.now().plusSeconds(60))
        val settings = UserSettings(null, NotificationPreferences(true, false, true, false, true), false, lastSyncAt = now)

        assertEquals(null, news.html)
        assertEquals(null, news.imageUrl)
        assertEquals(null, event.detailUrl)
        assertEquals(null, dashboard.profile)
        assertEquals(null, dashboard.nextEvent)
        assertEquals(emptyList<Grade>(), dashboard.latestGrades)
        assertEquals(emptyList<Absence>(), dashboard.recentAbsences)
        assertEquals(emptyList<Project>(), dashboard.dueProjects)
        assertEquals(now, dashboard.lastSyncAt)
        assertEquals("Alice", message.author)
        assertEquals("Hello", message.body)
        assertEquals(true, message.mine)
        assertEquals("reminder-1", reminder.id)
        assertEquals("Deadline", reminder.title)
        assertEquals(now, reminder.dueAt)
        assertEquals(ReminderKind.Deadline, reminder.kind)
        assertEquals("projects", reminder.route)
        assertEquals(1L, account.id)
        assertEquals("Calendar", account.displayName)
        assertEquals("account@example.com", account.accountName)
        assertEquals("user", session.username)
        assertEquals(null, session.refreshToken)
        assertEquals(false, session.biometricEnabled)
        assertEquals(now, session.issuedAt)
        assertEquals(true, session.refreshAfter.isAfter(now))
        assertEquals(false, session.isExpired)
        assertEquals(false, session.requiresRefresh)
        assertEquals(false, settings.biometricEnabled)
        assertEquals(ThemeMode.System, settings.themeMode)
        assertEquals(false, settings.dynamicColorEnabled)
        assertEquals(AgendaColorMode.Course, settings.agendaColorMode)
        assertEquals(DEFAULT_REFRESH_MINUTES, settings.refreshIntervals.events)
        assertEquals(NO_REMINDER_MINUTES, settings.classReminderLeadMinutes)
        assertEquals(NO_REMINDER_MINUTES, settings.deadlineReminderLeadMinutes)
        assertEquals(Feature.Notifications, Feature.valueOf("Notifications"))
    }
}
