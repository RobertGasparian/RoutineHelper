package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class DailySnapshotWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val snapshotOrchestrator: DailySnapshotOrchestrator,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            snapshotOrchestrator.finalizePreviousDay()
            Result.success()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
