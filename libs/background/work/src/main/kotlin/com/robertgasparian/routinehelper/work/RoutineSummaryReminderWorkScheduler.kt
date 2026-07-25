package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.robertgasparian.routinehelper.core.time.TimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineSummaryReminderWorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val timeProvider: TimeProvider,
) {
    fun scheduleRecurringReminders() {
        val workManager = WorkManager.getInstance(context)

        RoutineSummaryReminderWorkSchedules.recurringReminders(timeProvider.now()).forEach { schedule ->
            workManager.enqueueUniquePeriodicWork(
                schedule.uniqueWorkName,
                schedule.existingWorkPolicy,
                schedule.toWorkRequest(),
            )
        }
    }

    companion object {
        const val DAILY_REMINDER_WORK_NAME = "daily-summary-reminder"
        const val WEEKLY_REMINDER_WORK_NAME = "weekly-summary-reminder"
        const val REMINDER_WORK_TAG = "routine-summary-reminder"
    }
}

private fun RoutineSummaryReminderWorkSchedule.toWorkRequest(): PeriodicWorkRequest {
    val builder = when (workerKind) {
        SummaryReminderWorkerKind.Daily -> PeriodicWorkRequestBuilder<DailySummaryReminderWorker>(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = repeatIntervalUnit,
        )
        SummaryReminderWorkerKind.Weekly -> PeriodicWorkRequestBuilder<WeeklySummaryReminderWorker>(
            repeatInterval = repeatInterval,
            repeatIntervalTimeUnit = repeatIntervalUnit,
        )
    }

    return builder
        .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
        .addTag(tag)
        .build()
}
