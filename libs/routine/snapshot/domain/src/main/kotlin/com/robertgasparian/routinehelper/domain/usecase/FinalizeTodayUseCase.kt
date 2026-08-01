package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class FinalizeTodayUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(
        date: String,
        finalizedAtMillis: Long,
        snapshotPeriodStartDate: String = date,
    ): Long? {
        val todayItems = todayRoutineRepository.todayItems(date).first()
        val snapshotItems = todayItems.map { item ->
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

        val reflection = todayRoutineRepository.reflection(date).first()

        val snapshotId = routineHistoryRepository.saveSnapshot(
            periodStartDate = snapshotPeriodStartDate,
            finalizedAtMillis = finalizedAtMillis,
            items = snapshotItems,
            reflection = reflection,
            cadence = RoutineCadence.Daily,
        )
        return snapshotId
    }
}
