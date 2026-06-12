package com.elg.myges.adapters.secondary.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.elg.myges.R
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.Project
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {
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

    override suspend fun scheduleStudentSync() {
        val request = PeriodicWorkRequestBuilder<StudentSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_STUDENT_SYNC,
            ExistingPeriodicWorkPolicy.UPDATE,
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
            body = context.getString(R.string.notifications_sync_failed_body)
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
            )
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
            )
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
            )
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
            )
        )
    }

    override suspend fun showNewDocument(document: AcademicDocument) {
        showNotification(
            id = stableNotificationId("document:${document.id}"),
            title = context.getString(R.string.notifications_new_document_title),
            body = context.getString(
                R.string.notifications_new_document_body,
                document.title.ifBlank { context.getString(R.string.common_untitled) }
            )
        )
    }

    private fun showNotification(id: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_STUDENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
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
            .format(value)
    }

    private fun currentLocale(): Locale {
        return context.resources.configuration.locales[0] ?: Locale.getDefault()
    }

    private fun stableNotificationId(value: String): Int {
        return value.hashCode().let { if (it == Int.MIN_VALUE) Int.MAX_VALUE else kotlin.math.abs(it) }
    }

    private companion object {
        const val CHANNEL_STUDENT = "student"
        const val NOTIFICATION_SYNC_FAILURE = 1001
        const val WORK_STUDENT_SYNC = "student_sync"
    }
}
