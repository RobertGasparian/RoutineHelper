package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class UpdateTodayItemCompletedCountUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    suspend operator fun invoke(
        date: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        todayRoutineRepository.updateCompletedCount(
            date = date,
            routineItemId = routineItemId,
            completedCount = completedCount,
        )
    }
}
