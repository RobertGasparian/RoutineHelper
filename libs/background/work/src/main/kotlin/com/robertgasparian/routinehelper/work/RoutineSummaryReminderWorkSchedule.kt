package com.robertgasparian.routinehelper.work

import androidx.work.ExistingPeriodicWorkPolicy
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

internal data class RoutineSummaryReminderWorkSchedule(
    val uniqueWorkName: String,
    val existingWorkPolicy: ExistingPeriodicWorkPolicy,
    val workerKind: SummaryReminderWorkerKind,
    val repeatInterval: Long,
    val repeatIntervalUnit: TimeUnit,
    val initialDelay: Duration,
    val tag: String,
)

internal enum class SummaryReminderWorkerKind {
    Daily,
    Weekly,
}

internal object RoutineSummaryReminderWorkSchedules {
    fun recurringReminders(now: ZonedDateTime): List<RoutineSummaryReminderWorkSchedule> =
        listOf(
            RoutineSummaryReminderWorkSchedule(
                uniqueWorkName = RoutineSummaryReminderWorkScheduler.DAILY_REMINDER_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SummaryReminderWorkerKind.Daily,
                repeatInterval = 1,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = RoutineSummaryReminderWorkDates.delayUntilNextDailyReminder(now),
                tag = RoutineSummaryReminderWorkScheduler.REMINDER_WORK_TAG,
            ),
            RoutineSummaryReminderWorkSchedule(
                uniqueWorkName = RoutineSummaryReminderWorkScheduler.WEEKLY_REMINDER_WORK_NAME,
                existingWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                workerKind = SummaryReminderWorkerKind.Weekly,
                repeatInterval = 7,
                repeatIntervalUnit = TimeUnit.DAYS,
                initialDelay = RoutineSummaryReminderWorkDates.delayUntilNextWeeklyReminder(now),
                tag = RoutineSummaryReminderWorkScheduler.REMINDER_WORK_TAG,
            ),
        )
}
