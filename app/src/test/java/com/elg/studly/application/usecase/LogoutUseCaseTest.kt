package com.elg.studly.application.usecase

import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogoutUseCaseTest {

    @Test
    fun logsOutAndCancelsSync() = runTest {
        val sessionRepository = mockk<SessionRepository>(relaxed = true)
        val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)

        val useCase = LogoutUseCase(sessionRepository, notificationScheduler)

        useCase()

        coVerify { notificationScheduler.cancelStudentSync() }
        coVerify { sessionRepository.logout() }
    }
}
