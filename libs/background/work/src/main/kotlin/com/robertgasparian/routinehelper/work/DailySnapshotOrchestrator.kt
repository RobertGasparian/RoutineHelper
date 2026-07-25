package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DailySnapshotOrchestrator @Inject internal constructor(
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val resetTodayUseCase: ResetTodayUseCase,
    private val snapshotSummariesUseCase: SnapshotSummariesUseCase,
    private val timeProvider: TimeProvider,
) {
    private val finalizationMutex = Mutex()

    internal suspend fun finalizePreviousDay(now: ZonedDateTime = timeProvider.now()) {
        finalizationMutex.withLock {
            val snapshotPeriodStartDate = SnapshotDates.dailySnapshotDate(now).toString()
            val isAlreadyFinalized = snapshotSummariesUseCase(RoutineCadence.Daily)
                .first()
                .any { summary -> summary.periodStartDate == snapshotPeriodStartDate }
            if (isAlreadyFinalized) {
                // A previous attempt may have persisted the snapshot and died before cleanup.
                resetTodayUseCase(snapshotPeriodStartDate)
                return@withLock
            }

            finalizeTodayUseCase(
                date = snapshotPeriodStartDate,
                snapshotPeriodStartDate = snapshotPeriodStartDate,
                finalizedAtMillis = now.toInstant().toEpochMilli(),
            )
            resetTodayUseCase(snapshotPeriodStartDate)
        }
    }
}
