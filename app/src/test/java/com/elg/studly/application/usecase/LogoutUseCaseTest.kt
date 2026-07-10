package com.elg.studly.application.usecase

import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogoutUseCaseTest {

    @Test
    fun logsOutAndCancelsSync() = runTest {
        val sessionRepository = mockk<SessionRepository>(relaxed = true)
        val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)
        val studentDataRepository = mockk<StudentDataRepository>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)

        val useCase = LogoutUseCase(sessionRepository, notificationScheduler, studentDataRepository, settingsRepository)

        useCase()

        coVerify { notificationScheduler.cancelStudentSync() }
        coVerify { studentDataRepository.clearCache() }
        coVerify { settingsRepository.clearSyncMetadata() }
        coVerify { sessionRepository.logout() }
        coVerifyOrder {
            notificationScheduler.cancelStudentSync()
            studentDataRepository.clearCache()
            settingsRepository.clearSyncMetadata()
            sessionRepository.logout()
        }
    }
}
