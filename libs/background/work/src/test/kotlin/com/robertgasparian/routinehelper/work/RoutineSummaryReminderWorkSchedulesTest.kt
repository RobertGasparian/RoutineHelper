package com.robertgasparian.routinehelper.work

import androidx.work.ExistingPeriodicWorkPolicy
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineSummaryReminderWorkSchedulesTest {
    private val zoneId = ZoneId.of("America/New_York")

    @Test
    fun `given Friday morning when creating recurring schedules then daily and Monday reminder specs are returned`() {
        val now = ZonedDateTime.of(2026, 5, 29, 9, 0, 0, 0, zoneId)

        val schedules = RoutineSummaryReminderWorkSchedules.recurringReminders(now)

        assertEquals(
            RoutineSummaryReminderWorkSchedule(
                uniqueWorkName = RoutineSummaryReminderWorkScheduler.DAILY_REMINDER_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SummaryReminderWorkerKind.Daily,
                repeatInterval = 1,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = Duration.ofHours(1),
                tag = RoutineSummaryReminderWorkScheduler.REMINDER_WORK_TAG,
            ),
            schedules[0],
        )
        assertEquals(
            RoutineSummaryReminderWorkSchedule(
                uniqueWorkName = RoutineSummaryReminderWorkScheduler.WEEKLY_REMINDER_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SummaryReminderWorkerKind.Weekly,
                repeatInterval = 7,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = Duration.ofDays(3).plusHours(1),
                tag = RoutineSummaryReminderWorkScheduler.REMINDER_WORK_TAG,
            ),
            schedules[1],
        )
    }

    @Test
    fun `given Monday at reminder time when creating schedules then both target their next interval`() {
        val now = ZonedDateTime.of(2026, 6, 1, 10, 0, 0, 0, zoneId)

        val schedules = RoutineSummaryReminderWorkSchedules.recurringReminders(now)

        assertEquals(Duration.ofDays(1), schedules[0].initialDelay)
        assertEquals(Duration.ofDays(7), schedules[1].initialDelay)
    }
}
