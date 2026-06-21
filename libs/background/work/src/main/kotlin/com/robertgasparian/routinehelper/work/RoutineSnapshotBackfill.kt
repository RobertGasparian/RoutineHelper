package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RoutineSnapshotBackfill @Inject constructor(
    private val dailySnapshotOrchestrator: DailySnapshotOrchestrator,
    private val weeklySnapshotOrchestrator: WeeklySnapshotOrchestrator,
    private val timeProvider: TimeProvider,
) {
    suspend fun backfillMissedSnapshots(now: ZonedDateTime = timeProvider.now()) {
        if (SnapshotWorkDates.shouldBackfillDailyOnAppStart(now)) {
            attemptBackfill {
                dailySnapshotOrchestrator.finalizePreviousDay(now)
            }
        }

        if (SnapshotWorkDates.shouldBackfillWeeklyOnAppStart(now)) {
            attemptBackfill {
                weeklySnapshotOrchestrator.finalizePreviousWeek(now)
            }
        }
    }

    private suspend fun attemptBackfill(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            // WorkManager still owns the reliable retry path; app-start backfill should never crash startup.
        }
    }
}
