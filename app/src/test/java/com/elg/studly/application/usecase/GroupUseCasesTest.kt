package com.elg.studly.application.usecase

import com.elg.studly.application.ports.StudentDataRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GroupUseCasesTest {

    @Test
    fun joinGroupDelegatesToRepository() = runTest {
        val repository = mockk<StudentDataRepository>(relaxed = true)
        val useCase = JoinGroupUseCase(repository)

        useCase("courseId", "projectId", "groupId")

        coVerify { repository.joinGroup("courseId", "projectId", "groupId") }
    }

    @Test
    fun leaveGroupDelegatesToRepository() = runTest {
        val repository = mockk<StudentDataRepository>(relaxed = true)
        val useCase = LeaveGroupUseCase(repository)

        useCase("courseId", "projectId", "groupId")

        coVerify { repository.leaveGroup("courseId", "projectId", "groupId") }
    }
}
