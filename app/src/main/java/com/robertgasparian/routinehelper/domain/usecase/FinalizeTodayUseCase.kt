package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
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
        snapshotDate: String = date,
    ): Long {
        routineHistoryRepository.snapshotForDate(snapshotDate, cadence = RoutineCadence.Daily).first()?.let { existing ->
            todayRoutineRepository.resetDate(date)
            return existing.snapshotId
        }

        val todayItems = todayRoutineRepository.todayItems(date).first()
        val summaryNote = todayRoutineRepository.summaryNote(date).first()
        val snapshotItems = todayItems.map { item ->
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
            date = snapshotDate,
            finalizedAtMillis = finalizedAtMillis,
            items = snapshotItems,
            summaryNote = summaryNote,
            cadence = RoutineCadence.Daily,
        )
        todayRoutineRepository.resetDate(date)
        return snapshotId
    }
}
