package com.elg.studly.application.usecase

import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.ProjectMessage
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    @Test
    fun projectMessagesDelegatesToRepository() = runTest {
        val messages = listOf(ProjectMessage("message-1", "Alice", "Hello", null, false))
        val repository = mockk<StudentDataRepository> {
            coEvery { projectMessages("groupId") } returns messages
        }
        val useCase = ProjectMessagesUseCase(repository)

        assertEquals(messages, useCase("groupId"))
    }

    @Test
    fun sendProjectMessageDelegatesToRepository() = runTest {
        val repository = mockk<StudentDataRepository>(relaxed = true)
        val useCase = SendProjectMessageUseCase(repository)

        useCase("groupId", "Hello")

        coVerify { repository.sendProjectMessage("groupId", "Hello") }
    }
}
