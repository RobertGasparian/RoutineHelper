package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class WeeklySnapshotWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val snapshotOrchestrator: WeeklySnapshotOrchestrator,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            snapshotOrchestrator.finalizePreviousWeek()
            Result.success()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
