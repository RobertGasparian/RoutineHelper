package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineSnapshotWorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun scheduleRecurringSnapshots() {
        val now = ZonedDateTime.now()
        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniquePeriodicWork(
            DAILY_SNAPSHOT_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<DailySnapshotWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(
                    SnapshotWorkDates.delayUntilNextDailyFinalize(now).toMillis(),
                    TimeUnit.MILLISECONDS,
                )
                .addTag(SNAPSHOT_WORK_TAG)
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            WEEKLY_SNAPSHOT_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<WeeklySnapshotWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(
                    SnapshotWorkDates.delayUntilNextWeeklyFinalize(now).toMillis(),
                    TimeUnit.MILLISECONDS,
                )
                .addTag(SNAPSHOT_WORK_TAG)
                .build(),
        )
    }

    companion object {
        const val DAILY_SNAPSHOT_WORK_NAME = "daily-routine-snapshot"
        const val WEEKLY_SNAPSHOT_WORK_NAME = "weekly-routine-snapshot"
        const val SNAPSHOT_WORK_TAG = "routine-snapshot"
    }
}
