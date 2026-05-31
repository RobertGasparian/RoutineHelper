package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class FinalizeWeeklyUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        finalizedAtMillis: Long,
        snapshotWeekStartDate: String = weekStartDate,
    ): Long {
        routineHistoryRepository.snapshotForDate(
            date = snapshotWeekStartDate,
            cadence = RoutineCadence.Weekly,
        ).first()?.let { existing ->
            weeklyRoutineRepository.resetWeek(weekStartDate)
            return existing.snapshotId
        }

        val weeklyItems = weeklyRoutineRepository.weeklyItems(weekStartDate).first()
        val snapshotItems = weeklyItems.map { item ->
            RoutineDaySnapshotItem(
                actionId = item.actionId,
                title = item.title,
                description = item.description,
                repeatTargetCount = item.repeatTargetCount,
                completedCount = item.completedCount,
                position = item.position,
                isChecked = item.isChecked,
                note = item.note,
            )
        }

        val snapshotId = routineHistoryRepository.saveSnapshot(
            date = snapshotWeekStartDate,
            finalizedAtMillis = finalizedAtMillis,
            items = snapshotItems,
            cadence = RoutineCadence.Weekly,
        )
        weeklyRoutineRepository.resetWeek(weekStartDate)
        return snapshotId
    }
}
