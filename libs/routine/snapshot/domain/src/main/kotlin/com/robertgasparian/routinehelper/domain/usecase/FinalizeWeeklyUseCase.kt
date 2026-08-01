package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
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
        snapshotPeriodStartDate: String = weekStartDate,
    ): Long? {
        val weeklyItems = weeklyRoutineRepository.weeklyItems(weekStartDate).first()
        val snapshotItems = weeklyItems.map { item ->
            RoutineSnapshotItem(
                actionId = item.actionId,
                title = item.title,
                description = item.description,
                repeatTargetCount = item.repeatTargetCount,
                completedCount = item.completedCount,
                position = item.position,
                isChecked = item.isChecked,
                isHidden = item.isHidden,
                note = item.note,
            )
        }
        if (snapshotItems.isEmpty()) return null

        val reflection = weeklyRoutineRepository.reflection(weekStartDate).first()

        val snapshotId = routineHistoryRepository.saveSnapshot(
            periodStartDate = snapshotPeriodStartDate,
            finalizedAtMillis = finalizedAtMillis,
            items = snapshotItems,
            reflection = reflection,
            cadence = RoutineCadence.Weekly,
        )
        return snapshotId
    }
}
