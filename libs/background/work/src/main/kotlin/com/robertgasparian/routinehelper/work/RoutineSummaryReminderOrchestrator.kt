package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.AppSettings
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.AppSettingsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class RoutineSummaryReminderOrchestrator @Inject internal constructor(
    private val appSettingsUseCase: AppSettingsUseCase,
    private val snapshotSummariesUseCase: SnapshotSummariesUseCase,
    private val dailySnapshotOrchestrator: DailySnapshotOrchestrator,
    private val weeklySnapshotOrchestrator: WeeklySnapshotOrchestrator,
    private val notifier: RoutineSummaryReminderNotifier,
    private val timeProvider: TimeProvider,
) {
    internal suspend fun sendDailyReminder(now: ZonedDateTime = timeProvider.now()) {
        sendReminder(
            cadence = RoutineCadence.Daily,
            periodStartDate = SnapshotDates.dailySnapshotDate(now).toString(),
            isEnabled = AppSettings::isDailySummaryNotificationEnabled,
            finalizeSnapshot = { dailySnapshotOrchestrator.finalizePreviousDay(now) },
        )
    }

    internal suspend fun sendWeeklyReminder(now: ZonedDateTime = timeProvider.now()) {
        sendReminder(
            cadence = RoutineCadence.Weekly,
            periodStartDate = SnapshotDates.previousCompletedCalendarWeekStartDate(now).toString(),
            isEnabled = AppSettings::isWeeklySummaryNotificationEnabled,
            finalizeSnapshot = { weeklySnapshotOrchestrator.finalizePreviousWeek(now) },
        )
    }

    private suspend fun sendReminder(
        cadence: RoutineCadence,
        periodStartDate: String,
        isEnabled: (AppSettings) -> Boolean,
        finalizeSnapshot: suspend () -> Unit,
    ) {
        if (!isEnabled(appSettingsUseCase().first())) return

        // WorkManager does not guarantee ordering between the early snapshot job and this reminder.
        // Finalization is idempotent, so establish the completed-period state before deciding.
        finalizeSnapshot()
        val snapshot = snapshotSummariesUseCase(cadence)
            .first()
            .firstOrNull { summary -> summary.periodStartDate == periodStartDate }
        if (snapshot?.hasReflection == true) return

        // Empty finalized routines deliberately have no stored snapshot. That absence selects the
        // routine-building reminder instead of creating an unusable History destination.
        val notification = snapshot?.let { summary ->
            RoutineSummaryReminderNotification.WriteSummary(summary.snapshotId)
        } ?: RoutineSummaryReminderNotification.BuildRoutine(cadence)

        notifier.show(notification)
    }
}
