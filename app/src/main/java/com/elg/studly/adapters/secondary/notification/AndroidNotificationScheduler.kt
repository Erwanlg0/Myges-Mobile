package com.elg.studly.adapters.secondary.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elg.studly.MainActivity
import com.elg.studly.R
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ReminderKind
import com.elg.studly.domain.model.ReminderTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.time.Duration
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {
    private val appIcon: Bitmap? by lazy {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.toBitmap()
    }

    override fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_STUDENT,
            context.getString(R.string.notifications_channel_student),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notifications_channel_student_description)
        }
        manager.createNotificationChannel(channel)
    }

    override suspend fun scheduleStudentSync(intervalMinutes: Long) {
        val request = PeriodicWorkRequestBuilder<StudentSyncWorker>(
            intervalMinutes.coerceAtLeast(MIN_PERIODIC_MINUTES),
            TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_STUDENT_SYNC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override suspend fun runStudentSyncNow() {
        val request = OneTimeWorkRequestBuilder<StudentSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_STUDENT_SYNC_NOW,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override suspend fun cancelStudentSync() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_STUDENT_SYNC)
    }

    override suspend fun showSyncFailure() {
        showNotification(
            id = NOTIFICATION_SYNC_FAILURE,
            title = context.getString(R.string.notifications_sync_failed_title),
            body = context.getString(R.string.notifications_sync_failed_body),
            route = ROUTE_DASHBOARD
        )
    }

    override suspend fun showNewGrade(grade: Grade) {
        showNotification(
            id = stableNotificationId("grade:${grade.id}"),
            title = context.getString(R.string.notifications_new_grade_title),
            body = context.getString(
                R.string.notifications_new_grade_body,
                grade.courseName.ifBlank { context.getString(R.string.common_untitled) },
                formatGrade(grade)
            ),
            route = "$ROUTE_GRADES?id=${grade.id}",
            subject = grade.courseName
        )
    }

    override suspend fun showNewAbsence(absence: Absence) {
        showNotification(
            id = stableNotificationId("absence:${absence.id}"),
            title = context.getString(R.string.notifications_new_absence_title),
            body = context.getString(
                R.string.notifications_new_absence_body,
                absence.courseName.ifBlank { context.getString(R.string.common_untitled) },
                formatInstant(absence.startsAt)
            ),
            route = "$ROUTE_ABSENCES?id=${absence.id}",
            subject = absence.courseName
        )
    }

    override suspend fun showAgendaChange(event: AgendaEvent) {
        showNotification(
            id = stableNotificationId("agenda:${event.id}"),
            title = context.getString(R.string.notifications_agenda_changed_title),
            body = context.getString(
                R.string.notifications_agenda_changed_body,
                event.title.ifBlank { context.getString(R.string.common_untitled) },
                formatInstant(event.startsAt)
            ),
            route = "$ROUTE_AGENDA?id=${event.id}",
            subject = event.title
        )
    }

    override suspend fun showProjectDeadline(project: Project) {
        val deadline = project.deadline?.let(::formatInstant)
            ?: context.getString(R.string.common_no_period)
        showNotification(
            id = stableNotificationId("project:${project.id}"),
            title = context.getString(R.string.notifications_project_deadline_title),
            body = context.getString(
                R.string.notifications_project_deadline_body,
                project.name.ifBlank { context.getString(R.string.common_untitled) },
                deadline
            ),
            route = "$ROUTE_PROJECTS?id=${project.id}",
            subject = project.courseName ?: project.name
        )
    }

    override suspend fun scheduleReminders(
        targets: List<ReminderTarget>,
        classLeadMinutes: Int,
        deadlineLeadMinutes: Int
    ) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(EventReminderWorker.TAG)

        fun leadFor(kind: ReminderKind) = when (kind) {
            ReminderKind.Class -> classLeadMinutes
            ReminderKind.Deadline -> deadlineLeadMinutes
        }
        if (classLeadMinutes <= 0 && deadlineLeadMinutes <= 0) return

        val now = kotlin.time.Clock.System.now()
        val horizon = now + REMINDER_HORIZON_DAYS.days
        targets.asSequence()
            .filter { leadFor(it.kind) > 0 && it.dueAt.isAfter(now) && it.dueAt.isBefore(horizon) }
            .sortedBy { it.dueAt }
            .take(MAX_REMINDERS)
            .forEach { target ->
                val fireAt = target.dueAt - leadFor(target.kind).minutes
                val delayMillis = (fireAt - now).inWholeMilliseconds.coerceAtLeast(0)
                val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag(EventReminderWorker.TAG)
                    .setInputData(
                        workDataOf(
                            EventReminderWorker.KEY_ID to target.id,
                            EventReminderWorker.KEY_TITLE to target.title,
                            EventReminderWorker.KEY_DUE_AT to target.dueAt.toEpochMilli(),
                            EventReminderWorker.KEY_KIND to target.kind.name,
                            EventReminderWorker.KEY_ROUTE to target.route
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "$WORK_EVENT_REMINDER:${target.id}",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
    }

    fun showReminder(id: String, title: String, dueAtEpochMillis: Long, kind: ReminderKind, route: String) {
        val safeTitle = title.ifBlank { context.getString(R.string.common_untitled) }
        val time = formatInstant(Instant.fromEpochMilliseconds(dueAtEpochMillis))
        val (titleRes, bodyRes) = when (kind) {
            ReminderKind.Class -> R.string.notifications_event_reminder_title to R.string.notifications_event_reminder_body
            ReminderKind.Deadline -> R.string.notifications_deadline_reminder_title to R.string.notifications_deadline_reminder_body
        }
        showNotification(
            id = stableNotificationId("reminder:$id"),
            title = context.getString(titleRes),
            body = context.getString(bodyRes, safeTitle, time),
            route = route,
            subject = safeTitle
        )
    }

    override suspend fun showNewDocument(document: AcademicDocument) {
        showNotification(
            id = stableNotificationId("document:${document.id}"),
            title = context.getString(R.string.notifications_new_document_title),
            body = context.getString(
                R.string.notifications_new_document_body,
                document.title.ifBlank { context.getString(R.string.common_untitled) }
            ),
            route = "$ROUTE_DOCUMENTS?id=${document.id}",
            subject = document.category ?: document.title
        )
    }

    private fun showNotification(
        id: Int,
        title: String,
        body: String,
        route: String,
        subject: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = contentIntent(id, route)
        val notification = NotificationCompat.Builder(context, CHANNEL_STUDENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(appIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(intent)
            .setGroup(notificationGroupKey(route, subject))
            .addAction(
                R.mipmap.ic_launcher,
                context.getString(R.string.notifications_action_open),
                intent
            )
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun contentIntent(id: Int, route: String): PendingIntent {
        val intent = (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent())
            .setPackage(context.packageName)
            .putExtra(MainActivity.EXTRA_NOTIFICATION_ROUTE, route)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatGrade(grade: Grade): String {
        val value = grade.value ?: return context.getString(R.string.common_untitled)
        val scale = grade.scale ?: 20.0
        val formatter = NumberFormat.getNumberInstance(currentLocale()).apply {
            maximumFractionDigits = 2
        }
        return context.getString(
            R.string.grades_value_format,
            formatter.format(value),
            formatter.format(scale)
        )
    }

    private fun formatInstant(value: Instant): String {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(currentLocale())
            .withZone(ZoneId.systemDefault())
            .format(value.toJavaInstant())
    }

    private fun currentLocale(): Locale {
        return context.resources.configuration.locales[0] ?: Locale.getDefault()
    }

    private companion object {
        const val MIN_PERIODIC_MINUTES = 15L
        const val CHANNEL_STUDENT = "student"
        const val NOTIFICATION_SYNC_FAILURE = 1001
        const val WORK_STUDENT_SYNC = "student_sync"
        const val WORK_STUDENT_SYNC_NOW = "student_sync_now"
        const val WORK_EVENT_REMINDER = "event_reminder"
        const val REMINDER_HORIZON_DAYS = 14L
        const val MAX_REMINDERS = 100
        const val ROUTE_DASHBOARD = "dashboard"
        const val ROUTE_AGENDA = "agenda"
        const val ROUTE_GRADES = "grades"
        const val ROUTE_ABSENCES = "absences"
        const val ROUTE_PROJECTS = "projects"
        const val ROUTE_DOCUMENTS = "documents"
    }
}
