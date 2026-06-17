package com.elg.studly.adapters.secondary.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elg.studly.domain.model.ReminderKind
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class EventReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val scheduler: AndroidNotificationScheduler
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.success()
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val route = inputData.getString(KEY_ROUTE) ?: return Result.success()
        val dueAt = inputData.getLong(KEY_DUE_AT, 0L)
        val kind = runCatching { ReminderKind.valueOf(inputData.getString(KEY_KIND).orEmpty()) }
            .getOrDefault(ReminderKind.Class)
        scheduler.showReminder(id, title, dueAt, kind, route)
        return Result.success()
    }

    companion object {
        const val KEY_ID = "id"
        const val KEY_TITLE = "title"
        const val KEY_DUE_AT = "due_at"
        const val KEY_KIND = "kind"
        const val KEY_ROUTE = "route"
        const val TAG = "event_reminder"
    }
}
