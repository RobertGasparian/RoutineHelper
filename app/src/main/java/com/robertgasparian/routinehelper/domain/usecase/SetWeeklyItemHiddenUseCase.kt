package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class SetWeeklyItemHiddenUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        weeklyRoutineRepository.setHidden(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isHidden = isHidden,
        )
    }
}
