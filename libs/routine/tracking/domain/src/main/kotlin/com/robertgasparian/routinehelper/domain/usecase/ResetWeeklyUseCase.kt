package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class ResetWeeklyUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(weekStartDate: String) {
        weeklyRoutineRepository.resetWeek(weekStartDate)
    }
}
