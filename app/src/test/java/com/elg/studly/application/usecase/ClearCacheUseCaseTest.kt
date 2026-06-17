package com.elg.studly.application.usecase

import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearCacheUseCaseTest {

    @Test
    fun clearsCacheAndSyncMetadata() = runTest {
        val studentDataRepository = mockk<StudentDataRepository>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)

        val useCase = ClearCacheUseCase(studentDataRepository, settingsRepository)

        useCase()

        coVerify { studentDataRepository.clearCache() }
        coVerify { settingsRepository.clearSyncMetadata() }
    }
}
