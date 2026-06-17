package com.elg.studly.application.usecase

import com.elg.studly.application.ports.SessionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UnlockWithBiometricsUseCaseTest {

    @Test
    fun delegatesToRepository() = runTest {
        val repository = mockk<SessionRepository>(relaxed = true)

        val useCase = UnlockWithBiometricsUseCase(repository)
        useCase()

        coVerify { repository.unlockWithBiometrics() }
    }
}
