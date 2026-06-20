package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class SaveTemplateItemUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        actionId: Long?,
        title: String,
        description: String?,
        repeatTargetCount: Int? = null,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return

        val normalizedDescription = description?.trim()?.takeIf(String::isNotEmpty)
        val normalizedRepeatTargetCount = repeatTargetCount?.takeIf { it > 1 }
        if (actionId == null) {
            routineTemplateRepository.addTemplateItem(
                title = normalizedTitle,
                description = normalizedDescription,
                repeatTargetCount = normalizedRepeatTargetCount,
                cadence = cadence,
            )
        } else {
            routineTemplateRepository.updateAction(
                actionId = actionId,
                title = normalizedTitle,
                description = normalizedDescription,
                repeatTargetCount = normalizedRepeatTargetCount,
            )
        }
    }
}
