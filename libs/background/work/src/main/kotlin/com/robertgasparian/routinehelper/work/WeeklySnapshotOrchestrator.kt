package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class WeeklySnapshotOrchestrator @Inject internal constructor(
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val resetWeeklyUseCase: ResetWeeklyUseCase,
    private val snapshotSummariesUseCase: SnapshotSummariesUseCase,
    private val timeProvider: TimeProvider,
) {
    private val finalizationMutex = Mutex()

    internal suspend fun finalizePreviousWeek(now: ZonedDateTime = timeProvider.now()) {
        finalizationMutex.withLock {
            val snapshotPeriodStartDate =
                SnapshotDates.previousCompletedCalendarWeekStartDate(now).toString()
            val isAlreadyFinalized = snapshotSummariesUseCase(RoutineCadence.Weekly)
                .first()
                .any { summary -> summary.periodStartDate == snapshotPeriodStartDate }
            if (isAlreadyFinalized) {
                // A previous attempt may have persisted the snapshot and died before cleanup.
                resetWeeklyUseCase(snapshotPeriodStartDate)
                return@withLock
            }

            finalizeWeeklyUseCase(
                weekStartDate = snapshotPeriodStartDate,
                snapshotPeriodStartDate = snapshotPeriodStartDate,
                finalizedAtMillis = now.toInstant().toEpochMilli(),
            )
            resetWeeklyUseCase(snapshotPeriodStartDate)
        }
    }
}
