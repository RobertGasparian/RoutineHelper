package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TemplateItemUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    operator fun invoke(actionId: Long): Flow<RoutineTemplateItem?> =
        routineTemplateRepository.templateItem(actionId)
}
