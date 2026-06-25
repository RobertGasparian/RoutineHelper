package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class UpdateTemplateItemUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        actionId: Long,
        title: String,
        description: String?,
        repeatTargetCount: Int? = null,
    ) {
        val normalizedTitle = normalizeTemplateTitle(title) ?: return

        routineTemplateRepository.updateAction(
            actionId = actionId,
            title = normalizedTitle,
            description = normalizeTemplateDescription(description),
            repeatTargetCount = normalizeTemplateRepeatTargetCount(repeatTargetCount),
        )
    }
}
