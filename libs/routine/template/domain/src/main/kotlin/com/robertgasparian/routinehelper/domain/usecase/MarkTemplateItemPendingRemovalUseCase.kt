package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class MarkTemplateItemPendingRemovalUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        cadence: RoutineCadence,
        routineItemId: Long,
    ) {
        routineTemplateRepository.markPendingRemoval(
            cadence = cadence,
            routineItemId = routineItemId,
        )
    }
}
