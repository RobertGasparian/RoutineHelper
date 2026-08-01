package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class WeeklyReflectionUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    operator fun invoke(weekStartDate: String) = weeklyRoutineRepository.reflection(weekStartDate)
}
