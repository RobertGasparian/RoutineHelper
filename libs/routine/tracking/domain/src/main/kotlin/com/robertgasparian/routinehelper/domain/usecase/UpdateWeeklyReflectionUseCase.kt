package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class UpdateWeeklyReflectionUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        reflection: RoutineReflection,
    ) {
        weeklyRoutineRepository.updateReflection(
            weekStartDate = weekStartDate,
            reflection = reflection,
        )
    }
}
