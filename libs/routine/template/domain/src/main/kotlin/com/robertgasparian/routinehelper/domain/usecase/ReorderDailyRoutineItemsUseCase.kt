package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class ReorderDailyRoutineItemsUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(routineItemIdsInOrder: List<Long>) {
        routineTemplateRepository.reorderTemplateItems(
            cadence = RoutineCadence.Daily,
            routineItemIdsInOrder = routineItemIdsInOrder,
        )
    }
}
