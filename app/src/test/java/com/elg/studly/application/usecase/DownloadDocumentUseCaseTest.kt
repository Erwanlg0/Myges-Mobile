package com.elg.studly.application.usecase

import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.AcademicDocument
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import android.net.Uri

class DownloadDocumentUseCaseTest {

    @Test
    fun delegatesToRepository() = runTest {
        val repository = mockk<StudentDataRepository>()
        val document = mockk<AcademicDocument>()
        val expectedUri = mockk<Uri>()
        val onProgress: (Float?) -> Unit = {}

        coEvery { repository.downloadDocument(document, onProgress) } returns expectedUri

        val useCase = DownloadDocumentUseCase(repository)
        val result = useCase(document, onProgress)

        assertEquals(expectedUri, result)
        coVerify { repository.downloadDocument(document, onProgress) }
    }

    @Test
    fun delegatesToRepositoryWithDefaultProgress() = runTest {
        val repository = mockk<StudentDataRepository>()
        val document = mockk<AcademicDocument>()
        val expectedUri = mockk<Uri>()

        coEvery { repository.downloadDocument(document, any()) } coAnswers {
            secondArg<(Float?) -> Unit>().invoke(null)
            expectedUri
        }

        val useCase = DownloadDocumentUseCase(repository)
        val result = useCase(document)

        assertEquals(expectedUri, result)
        coVerify { repository.downloadDocument(document, any()) }
    }
}
