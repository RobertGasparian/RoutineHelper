package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class UpdateWeeklyItemCompletedCountUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        weeklyRoutineRepository.updateCompletedCount(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            completedCount = completedCount,
        )
    }
}
