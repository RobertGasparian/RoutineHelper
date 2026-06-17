package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RoutineSnapshotBackfill @Inject constructor(
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val resetTodayUseCase: ResetTodayUseCase,
    private val resetWeeklyUseCase: ResetWeeklyUseCase,
    private val timeProvider: TimeProvider,
) {
    suspend fun backfillMissedSnapshots(now: ZonedDateTime = timeProvider.now()) {
        if (SnapshotWorkDates.shouldBackfillDailyOnAppStart(now)) {
            val snapshotDate = SnapshotWorkDates.dailySnapshotDate(now).toString()
            attemptBackfill {
                finalizeTodayUseCase(
                    date = snapshotDate,
                    snapshotDate = snapshotDate,
                    finalizedAtMillis = timeProvider.currentTimeMillis(),
                )
                resetTodayUseCase(snapshotDate)
            }
        }

        if (SnapshotWorkDates.shouldBackfillWeeklyOnAppStart(now)) {
            val snapshotWeekStartDate = SnapshotWorkDates.previousCompletedCalendarWeekStartDate(now).toString()
            attemptBackfill {
                finalizeWeeklyUseCase(
                    weekStartDate = snapshotWeekStartDate,
                    snapshotWeekStartDate = snapshotWeekStartDate,
                    finalizedAtMillis = timeProvider.currentTimeMillis(),
                )
                resetWeeklyUseCase(snapshotWeekStartDate)
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
