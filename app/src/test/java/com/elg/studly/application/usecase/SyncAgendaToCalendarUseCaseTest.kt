package com.elg.studly.application.usecase

import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.domain.model.AgendaEvent
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncAgendaToCalendarUseCaseTest {

    @Test
    fun delegatesToCalendarSyncPort() = runTest {
        val calendarSyncPort = mockk<CalendarSyncPort>(relaxed = true)
        val useCase = SyncAgendaToCalendarUseCase(calendarSyncPort)

        val events = listOf<AgendaEvent>(mockk())
        useCase(events)

        coVerify { calendarSyncPort.sync(events) }
    }
}
