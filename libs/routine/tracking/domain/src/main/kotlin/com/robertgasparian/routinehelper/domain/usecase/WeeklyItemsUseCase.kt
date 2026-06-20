package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class WeeklyItemsUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    operator fun invoke(weekStartDate: String): Flow<List<WeeklyRoutineItem>> =
        weeklyRoutineRepository.weeklyItems(weekStartDate)
}
