package com.elg.studly.adapters.secondary.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.datetime.LocalDate
import com.elg.studly.adapters.time.toJavaLocalDate
import java.time.ZoneOffset

class AgendaWindowTest {
    @Test
    fun agendaWindowCoversStrictFourWeeks() {
        val today = LocalDate.parse("2026-06-12")
        val window = AgendaWindow.fromToday(today)

        assertEquals(today.toJavaLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(), window.start)
        assertEquals(
            today.toJavaLocalDate().plusDays(27)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            window.end
        )
    }

    @Test
    fun agendaWindowFirstSyncCoversAcademicStartTo365DaysAhead() {
        val today = LocalDate.parse("2026-06-12")
        val window = AgendaWindow.firstSync(today)

        assertEquals(java.time.LocalDate.of(2023, 9, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(), window.start)
        assertEquals(
            today.toJavaLocalDate().plusDays(365)
                .atTime(23, 59, 59, 999_000_000)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            window.end
        )
    }

    @Test
    fun agendaWindowSubsequentSyncCoversYesterdayTo365DaysAhead() {
        val today = LocalDate.parse("2026-06-12")
        val window = AgendaWindow.subsequentSync(today)

        assertEquals(today.toJavaLocalDate().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(), window.start)
        assertEquals(
            today.toJavaLocalDate().plusDays(365)
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
