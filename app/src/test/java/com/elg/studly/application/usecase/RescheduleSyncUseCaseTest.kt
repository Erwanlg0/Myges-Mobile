package com.elg.studly.application.usecase

import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.RefreshIntervals
import com.elg.studly.domain.model.UserSettings
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RescheduleSyncUseCaseTest {

    @Test
    fun delegatesToSchedulerWithSmallestInterval() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)
        
        val intervals = mockk<RefreshIntervals>()
        every { intervals.smallestIntervalMinutes() } returns 15
        
        val settings = mockk<UserSettings>()
        every { settings.refreshIntervals } returns intervals
        
        every { settingsRepository.settings } returns MutableStateFlow(settings)

        val useCase = RescheduleSyncUseCase(settingsRepository, notificationScheduler)
        useCase()

        coVerify { notificationScheduler.scheduleStudentSync(15L) }
    }
}
