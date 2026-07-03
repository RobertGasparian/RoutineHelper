package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.robertgasparian.routinehelper.core.time.TimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineSnapshotWorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val timeProvider: TimeProvider,
) {
    fun scheduleRecurringSnapshots() {
        val workManager = WorkManager.getInstance(context)

        RoutineSnapshotWorkSchedules.recurringSnapshots(timeProvider.now()).forEach { schedule ->
            workManager.enqueueUniquePeriodicWork(
                schedule.uniqueWorkName,
                schedule.existingWorkPolicy,
                schedule.toWorkRequest(),
            )
        }
    }

    companion object {
        const val DAILY_SNAPSHOT_WORK_NAME = "daily-routine-snapshot"
        const val WEEKLY_SNAPSHOT_WORK_NAME = "weekly-routine-snapshot"
        const val SNAPSHOT_WORK_TAG = "routine-snapshot"
    }
}

private fun RoutineSnapshotWorkSchedule.toWorkRequest(): PeriodicWorkRequest {
    val builder = when (workerKind) {
        SnapshotWorkerKind.Daily -> PeriodicWorkRequestBuilder<DailySnapshotWorker>(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = repeatIntervalUnit,
        )
        SnapshotWorkerKind.Weekly -> PeriodicWorkRequestBuilder<WeeklySnapshotWorker>(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = repeatIntervalUnit,
        )
    }

    return builder
        .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
        .addTag(tag)
        .build()
}
