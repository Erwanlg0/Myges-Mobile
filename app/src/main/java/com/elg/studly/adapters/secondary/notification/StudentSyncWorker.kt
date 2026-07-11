package com.elg.studly.adapters.secondary.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elg.studly.adapters.primary.widget.WidgetUpdater
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.usecase.RefreshStudentDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class StudentSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val refreshStudentDataUseCase: RefreshStudentDataUseCase,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            refreshStudentDataUseCase()
            WidgetUpdater.refreshAll(applicationContext)
            Result.success()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            notificationScheduler.showSyncFailure()
            Result.retry()
        }
    }
}
