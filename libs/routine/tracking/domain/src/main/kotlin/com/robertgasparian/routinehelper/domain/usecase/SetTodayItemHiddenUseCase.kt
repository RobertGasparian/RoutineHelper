package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class SetTodayItemHiddenUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    suspend operator fun invoke(
        date: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        todayRoutineRepository.setHidden(
            date = date,
            routineItemId = routineItemId,
            isHidden = isHidden,
        )
    }
}
