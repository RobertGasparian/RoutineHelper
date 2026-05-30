package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TodayItemsUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    operator fun invoke(date: String): Flow<List<TodayRoutineItem>> =
        todayRoutineRepository.todayItems(date, cadence = RoutineCadence.Daily)
}
