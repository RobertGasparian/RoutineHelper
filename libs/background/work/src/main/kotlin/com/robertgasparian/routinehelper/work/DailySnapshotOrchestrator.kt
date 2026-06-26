package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import java.time.ZonedDateTime
import javax.inject.Inject

class DailySnapshotOrchestrator @Inject internal constructor(
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val resetTodayUseCase: ResetTodayUseCase,
    private val timeProvider: TimeProvider,
) {
    internal suspend fun finalizePreviousDay(now: ZonedDateTime = timeProvider.now()) {
        val snapshotPeriodStartDate = SnapshotDates.dailySnapshotDate(now).toString()
        finalizeTodayUseCase(
            date = snapshotPeriodStartDate,
            snapshotPeriodStartDate = snapshotPeriodStartDate,
            finalizedAtMillis = now.toInstant().toEpochMilli(),
        )
        resetTodayUseCase(snapshotPeriodStartDate)
    }
}
