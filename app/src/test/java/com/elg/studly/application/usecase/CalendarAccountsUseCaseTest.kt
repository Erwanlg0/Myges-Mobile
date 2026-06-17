package com.elg.studly.application.usecase

import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.domain.model.CalendarAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarAccountsUseCaseTest {

    @Test
    fun availableReturnsCalendars() = runTest {
        val port = mockk<CalendarSyncPort>()
        val calendars = listOf<CalendarAccount>(mockk())
        coEvery { port.availableCalendars() } returns calendars

        val useCase = CalendarAccountsUseCase(port)
        val result = useCase.available()

        assertEquals(calendars, result)
        coVerify { port.availableCalendars() }
    }

    @Test
    fun selectedReturnsCalendarId() = runTest {
        val port = mockk<CalendarSyncPort>()
        val id = 123L
        coEvery { port.selectedCalendarId() } returns id

        val useCase = CalendarAccountsUseCase(port)
        val result = useCase.selected()

        assertEquals(id, result)
        coVerify { port.selectedCalendarId() }
    }

    @Test
    fun selectSetsCalendarId() = runTest {
        val port = mockk<CalendarSyncPort>(relaxed = true)
        val id = 123L

        val useCase = CalendarAccountsUseCase(port)
        useCase.select(id)

        coVerify { port.selectCalendar(id) }
    }
}
