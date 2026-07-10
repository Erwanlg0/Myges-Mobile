package com.elg.studly.application.usecase

import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ReminderKind
import com.elg.studly.domain.model.ReminderTarget
import com.elg.studly.domain.model.SyncFeature
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class CompleteOAuthLoginUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        sessionRepository.authenticateWithToken(accessToken, expiresAt, enableBiometric)
        notificationScheduler.ensureChannels()
        val intervalMinutes = settingsRepository.settings.first().refreshIntervals.smallestIntervalMinutes().toLong()
        notificationScheduler.scheduleStudentSync(intervalMinutes)
        notificationScheduler.runStudentSyncNow()
    }
}

class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke() {
        notificationScheduler.cancelStudentSync()
        sessionRepository.logout()
    }
}

class RefreshStudentDataUseCase @Inject constructor(
    private val repository: StudentDataRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarSyncPort: CalendarSyncPort,
    private val notificationScheduler: NotificationScheduler
) {

    suspend operator fun invoke(force: Boolean = false, features: Set<SyncFeature>? = null) {
        repository.syncAll(force, features)
        val settings = settingsRepository.settings.first()
        if (settings.calendarSyncEnabled) {
            runCatching { calendarSyncPort.sync(repository.observeAgenda().first()) }
        }
        runCatching {
            notificationScheduler.scheduleReminders(
                buildReminderTargets(
                    agenda = repository.observeAgenda().first(),
                    projects = repository.observeProjects().first(),
                    practicals = repository.observePracticals().first()
                ),
                classLeadMinutes = settings.classReminderLeadMinutes,
                deadlineLeadMinutes = settings.deadlineReminderLeadMinutes
            )
        }
        settingsRepository.markSynced()
    }
}


internal fun buildReminderTargets(
    agenda: List<AgendaEvent>,
    projects: List<Project>,
    practicals: List<Practical>
): List<ReminderTarget> = buildList {
    agenda.forEach { event ->
        add(ReminderTarget("agenda:${event.id}", event.title, event.startsAt, ReminderKind.Class, "agenda?id=${event.id}"))
    }
    projects.forEach { project ->
        val datedSteps = project.steps.filter { it.deadline != null }
        if (datedSteps.isEmpty()) {
            project.deadline?.let { due ->
                add(ReminderTarget("project:${project.id}", project.name, due, ReminderKind.Deadline, "projects?id=${project.id}"))
            }
        }
        datedSteps.forEach { step ->
            add(ReminderTarget("project-step:${step.id}", stepLabel(project.name, step.title), step.deadline!!, ReminderKind.Deadline, "projects?id=${project.id}"))
        }
    }
    practicals.forEach { practical ->
        practical.startsAt?.let { start ->
            add(ReminderTarget("practical:${practical.id}", practical.name, start, ReminderKind.Class, "practicals"))
        }
        practical.steps.filter { it.deadline != null }.forEach { step ->
            add(ReminderTarget("practical-step:${step.id}", stepLabel(practical.name, step.title), step.deadline!!, ReminderKind.Deadline, "practicals"))
        }
    }
}

private fun stepLabel(parent: String, step: String): String =
    listOf(parent, step).filter { it.isNotBlank() }.joinToString(" · ")

class ClearCacheUseCase @Inject constructor(
    private val repository: StudentDataRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.clearCache()
        settingsRepository.clearSyncMetadata()
    }
}

class UpdateReminderLeadUseCase @Inject constructor(
    private val repository: SettingsRepository,
    private val studentDataRepository: StudentDataRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend fun classReminderLead(minutes: Int) {
        repository.setClassReminderLeadMinutes(minutes)
        rescheduleReminders()
    }

    suspend fun deadlineReminderLead(minutes: Int) {
        repository.setDeadlineReminderLeadMinutes(minutes)
        rescheduleReminders()
    }

    private suspend fun rescheduleReminders() {
        val settings = repository.settings.first()
        runCatching {
            notificationScheduler.scheduleReminders(
                buildReminderTargets(
                    agenda = studentDataRepository.observeAgenda().first(),
                    projects = studentDataRepository.observeProjects().first(),
                    practicals = studentDataRepository.observePracticals().first()
                ),
                classLeadMinutes = settings.classReminderLeadMinutes,
                deadlineLeadMinutes = settings.deadlineReminderLeadMinutes
            )
        }
    }
}


class RescheduleSyncUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke() {
        val intervalMinutes = settingsRepository.settings.first().refreshIntervals.smallestIntervalMinutes().toLong()
        notificationScheduler.scheduleStudentSync(intervalMinutes)
    }
}
