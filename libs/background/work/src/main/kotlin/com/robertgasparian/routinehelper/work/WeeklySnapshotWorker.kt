package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeeklySnapshotWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val snapshotOrchestrator: WeeklySnapshotOrchestrator,
    private val workerResultRunner: WorkerResultRunner,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result =
        workerResultRunner.run {
            snapshotOrchestrator.finalizePreviousWeek()
        }
}
