package com.robertgasparian.routinehelper.work

import androidx.work.ExistingPeriodicWorkPolicy
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineSnapshotWorkSchedulesTest {
    private val zoneId = ZoneId.of("America/New_York")

    @Test
    fun `given current time when creating recurring schedules then daily and weekly work specs are returned`() {
        val now = ZonedDateTime.of(2026, 5, 29, 1, 30, 0, 0, zoneId)

        val schedules = RoutineSnapshotWorkSchedules.recurringSnapshots(now)

        assertEquals(listOf(SnapshotWorkerKind.Daily, SnapshotWorkerKind.Weekly), schedules.map { it.workerKind })
        assertEquals(
            RoutineSnapshotWorkSchedule(
                uniqueWorkName = RoutineSnapshotWorkScheduler.DAILY_SNAPSHOT_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SnapshotWorkerKind.Daily,
                repeatInterval = 1,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = Duration.ofHours(1),
                tag = RoutineSnapshotWorkScheduler.SNAPSHOT_WORK_TAG,
            ),
            schedules[0],
        )
        assertEquals(
            RoutineSnapshotWorkSchedule(
                uniqueWorkName = RoutineSnapshotWorkScheduler.WEEKLY_SNAPSHOT_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SnapshotWorkerKind.Weekly,
                repeatInterval = 7,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = Duration.ofDays(3).plusHours(1),
                tag = RoutineSnapshotWorkScheduler.SNAPSHOT_WORK_TAG,
            ),
            schedules[1],
        )
    }
}
