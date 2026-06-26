package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import java.time.ZonedDateTime
import javax.inject.Inject

class WeeklySnapshotOrchestrator @Inject internal constructor(
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val resetWeeklyUseCase: ResetWeeklyUseCase,
    private val timeProvider: TimeProvider,
) {
    internal suspend fun finalizePreviousWeek(now: ZonedDateTime = timeProvider.now()) {
        val snapshotPeriodStartDate = SnapshotDates.previousCompletedCalendarWeekStartDate(now).toString()
        finalizeWeeklyUseCase(
            weekStartDate = snapshotPeriodStartDate,
            snapshotPeriodStartDate = snapshotPeriodStartDate,
            finalizedAtMillis = now.toInstant().toEpochMilli(),
        )
        resetWeeklyUseCase(snapshotPeriodStartDate)
    }
}
