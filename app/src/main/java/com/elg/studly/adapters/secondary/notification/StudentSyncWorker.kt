package com.elg.studly.adapters.secondary.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.usecase.RefreshStudentDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StudentSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val refreshStudentDataUseCase: RefreshStudentDataUseCase,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        return runCatching { refreshStudentDataUseCase() }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                notificationScheduler.showSyncFailure()
                Result.retry()
            }
        )
    }
}
