package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import java.time.ZonedDateTime
import javax.inject.Inject

class RoutineSnapshotFinalizer @Inject internal constructor(
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val resetTodayUseCase: ResetTodayUseCase,
    private val resetWeeklyUseCase: ResetWeeklyUseCase,
    private val timeProvider: TimeProvider,
) {
    internal suspend fun finalizeDaily(now: ZonedDateTime = timeProvider.now()) {
        val snapshotDate = SnapshotDates.dailySnapshotDate(now).toString()
        finalizeTodayUseCase(
            date = snapshotDate,
            snapshotDate = snapshotDate,
            finalizedAtMillis = timeProvider.currentTimeMillis(),
        )
        resetTodayUseCase(snapshotDate)
    }

    internal suspend fun finalizeWeekly(now: ZonedDateTime = timeProvider.now()) {
        val snapshotWeekStartDate = SnapshotDates.previousCompletedCalendarWeekStartDate(now).toString()
        finalizeWeeklyUseCase(
            weekStartDate = snapshotWeekStartDate,
            snapshotWeekStartDate = snapshotWeekStartDate,
            finalizedAtMillis = timeProvider.currentTimeMillis(),
        )
        resetWeeklyUseCase(snapshotWeekStartDate)
    }
}
