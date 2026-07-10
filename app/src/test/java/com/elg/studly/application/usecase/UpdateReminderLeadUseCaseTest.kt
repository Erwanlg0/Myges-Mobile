package com.elg.studly.application.usecase

import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.UserSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateReminderLeadUseCaseTest {

    @Test
    fun rescheduleRemindersOnLeadTimeChange() = runTest {
        val repository = mockk<SettingsRepository>(relaxed = true)
        val studentDataRepository = mockk<StudentDataRepository>(relaxed = true) {
            every { observeAgenda() } returns MutableStateFlow(emptyList())
            every { observeProjects() } returns MutableStateFlow(emptyList())
            every { observePracticals() } returns MutableStateFlow(emptyList())
        }
        val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)

        val settings = mockk<UserSettings> {
            every { classReminderLeadMinutes } returns 15
            every { deadlineReminderLeadMinutes } returns 60
        }
        every { repository.settings } returns MutableStateFlow(settings)

        val useCase = UpdateReminderLeadUseCase(repository, studentDataRepository, notificationScheduler)

        useCase.classReminderLead(15)
        coVerify { repository.setClassReminderLeadMinutes(15) }
        coVerify { notificationScheduler.scheduleReminders(any(), 15, 60) }

        useCase.deadlineReminderLead(60)
        coVerify { repository.setDeadlineReminderLeadMinutes(60) }
        coVerify(exactly = 2) { notificationScheduler.scheduleReminders(any(), 15, 60) }
    }

    @Test
    fun rescheduleReminderFailureIsIgnored() = runTest {
        val repository = mockk<SettingsRepository>(relaxed = true)
        val studentDataRepository = mockk<StudentDataRepository>(relaxed = true) {
            every { observeAgenda() } returns MutableStateFlow(emptyList())
            every { observeProjects() } returns MutableStateFlow(emptyList())
            every { observePracticals() } returns MutableStateFlow(emptyList())
        }
        val notificationScheduler = mockk<NotificationScheduler>(relaxed = true) {
            coEvery { scheduleReminders(any(), any(), any()) } throws IllegalStateException("failed")
        }
        val settings = mockk<UserSettings> {
            every { classReminderLeadMinutes } returns 15
            every { deadlineReminderLeadMinutes } returns 60
        }
        every { repository.settings } returns MutableStateFlow(settings)
        val useCase = UpdateReminderLeadUseCase(repository, studentDataRepository, notificationScheduler)

        useCase.classReminderLead(15)

        coVerify { repository.setClassReminderLeadMinutes(15) }
        coVerify { notificationScheduler.scheduleReminders(any(), 15, 60) }
    }
}
