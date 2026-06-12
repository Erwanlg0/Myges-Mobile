package com.elg.myges.adapters.secondary.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class AgendaWindowTest {
    @Test
    fun agendaWindowCoversStrictFourWeeks() {
        val today = LocalDate.parse("2026-06-12")
        val window = AgendaWindow.fromToday(today)

        assertEquals(today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(), window.start)
        assertEquals(
            today.plusDays(27)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            window.end
        )
    }

    @Test
    fun academicYearCandidatesContinueDescendingWhenCurrentYearIsEmpty() {
        val years = academicYearCandidates(listOf("2026"), null, currentYear = 2026)

        assertEquals(listOf("2026", "2025", "2024", "2023", "2022"), years.take(5))
    }

    @Test
    fun academicYearCandidatesPreferNewestYearWhenApiOrderIsOldestFirst() {
        val years = academicYearCandidates(listOf("2023", "2024"), "2025-ESGI-123", currentYear = 2026)

        assertEquals(listOf("2026", "2025", "2024", "2023"), years.take(4))
    }
}
