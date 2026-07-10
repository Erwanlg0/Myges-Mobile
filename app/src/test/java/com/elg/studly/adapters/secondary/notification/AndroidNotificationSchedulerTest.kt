package com.elg.studly.adapters.secondary.notification

import android.content.Context
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.impl.utils.futures.SettableFuture
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AndroidNotificationSchedulerTest {
    @Test
    fun cancelStudentSyncCancelsPeriodicImmediateAndReminderWork() = runTest {
        val context = mockk<Context>()
        val workManager = mockk<WorkManager>(relaxed = true)
        val completedResult = SettableFuture.create<Operation.State.SUCCESS>().apply { set(Operation.SUCCESS) }
        val completedOperation = mockk<Operation> {
            every { result } returns completedResult
        }
        mockkObject(WorkManager)
        every { WorkManager.getInstance(context) } returns workManager
        every { workManager.cancelUniqueWork(any()) } returns completedOperation
        every { workManager.cancelAllWorkByTag(any()) } returns completedOperation

        try {
            AndroidNotificationScheduler(context).cancelStudentSync()

            verify { workManager.cancelUniqueWork("student_sync") }
            verify { workManager.cancelUniqueWork("student_sync_now") }
            verify { workManager.cancelAllWorkByTag(EventReminderWorker.TAG) }
        } finally {
            unmockkObject(WorkManager)
        }
    }
}
