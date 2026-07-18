package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class DeleteTemplatePendingRemovalsUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        cadence: RoutineCadence,
        routineItemIds: List<Long>,
    ) {
        routineTemplateRepository.deletePendingRemovals(
            cadence = cadence,
            routineItemIds = routineItemIds,
        )
    }
}
