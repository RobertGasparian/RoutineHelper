package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RoutineSnapshotBackfill @Inject constructor(
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
) {
    suspend fun backfillMissedSnapshots(now: ZonedDateTime = ZonedDateTime.now()) {
        if (SnapshotWorkDates.shouldBackfillDailyOnAppStart(now)) {
            val snapshotDate = SnapshotWorkDates.dailySnapshotDate(now).toString()
            attemptBackfill {
                finalizeTodayUseCase(
                    date = snapshotDate,
                    snapshotDate = snapshotDate,
                    finalizedAtMillis = System.currentTimeMillis(),
                )
            }
        }

        if (SnapshotWorkDates.shouldBackfillWeeklyOnAppStart(now)) {
            val snapshotWeekStartDate = SnapshotWorkDates.previousCompletedCalendarWeekStartDate(now).toString()
            attemptBackfill {
                finalizeWeeklyUseCase(
                    weekStartDate = snapshotWeekStartDate,
                    snapshotWeekStartDate = snapshotWeekStartDate,
                    finalizedAtMillis = System.currentTimeMillis(),
                )
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
