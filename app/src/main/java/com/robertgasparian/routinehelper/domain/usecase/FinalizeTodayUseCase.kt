package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import kotlinx.coroutines.flow.first

class FinalizeTodayUseCase(
    private val todayRoutineRepository: TodayRoutineRepository,
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(
        date: String,
        finalizedAtMillis: Long,
    ): Long {
        routineHistoryRepository.snapshotForDate(date).first()?.let { existing ->
            todayRoutineRepository.resetDate(date)
            return existing.snapshotId
        }

        val todayItems = todayRoutineRepository.todayItems(date).first()
        val snapshotItems = todayItems.map { item ->
            RoutineDaySnapshotItem(
                actionId = item.actionId,
                title = item.title,
                description = item.description,
                position = item.position,
                isChecked = item.isChecked,
                note = item.note,
            )
        }

        val snapshotId = routineHistoryRepository.saveSnapshot(
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = snapshotItems,
        )
        todayRoutineRepository.resetDate(date)
        return snapshotId
    }
}
