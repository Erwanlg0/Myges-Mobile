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
}
