package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class SetTodayItemCheckedUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    suspend operator fun invoke(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        todayRoutineRepository.setChecked(
            date = date,
            routineItemId = routineItemId,
            isChecked = isChecked,
        )
    }
}
