package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class SetWeeklyItemCheckedUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        weeklyRoutineRepository.setChecked(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isChecked = isChecked,
        )
    }
}
