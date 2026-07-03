package com.robertgasparian.routinehelper.work

import androidx.work.ExistingPeriodicWorkPolicy
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

internal data class RoutineSnapshotWorkSchedule(
    val uniqueWorkName: String,
    val existingWorkPolicy: ExistingPeriodicWorkPolicy,
    val workerKind: SnapshotWorkerKind,
    val repeatInterval: Long,
    val repeatIntervalUnit: TimeUnit,
    val initialDelay: Duration,
    val tag: String,
)

internal enum class SnapshotWorkerKind {
    Daily,
    Weekly,
}

internal object RoutineSnapshotWorkSchedules {
    fun recurringSnapshots(now: ZonedDateTime): List<RoutineSnapshotWorkSchedule> =
        listOf(
            RoutineSnapshotWorkSchedule(
                uniqueWorkName = RoutineSnapshotWorkScheduler.DAILY_SNAPSHOT_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SnapshotWorkerKind.Daily,
                repeatInterval = 1,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = SnapshotWorkDates.delayUntilNextDailyFinalize(now),
                tag = RoutineSnapshotWorkScheduler.SNAPSHOT_WORK_TAG,
            ),
            RoutineSnapshotWorkSchedule(
                uniqueWorkName = RoutineSnapshotWorkScheduler.WEEKLY_SNAPSHOT_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SnapshotWorkerKind.Weekly,
                repeatInterval = 7,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = SnapshotWorkDates.delayUntilNextWeeklyFinalize(now),
                tag = RoutineSnapshotWorkScheduler.SNAPSHOT_WORK_TAG,
            ),
        )
}
