package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class SaveTemplateItemUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        actionId: Long?,
        title: String,
        description: String?,
    ) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return

        val normalizedDescription = description?.trim()?.takeIf(String::isNotEmpty)
        if (actionId == null) {
            routineTemplateRepository.addTemplateItem(
                title = normalizedTitle,
                description = normalizedDescription,
            )
        } else {
            routineTemplateRepository.updateAction(
                actionId = actionId,
                title = normalizedTitle,
                description = normalizedDescription,
            )
        }
    }
}
