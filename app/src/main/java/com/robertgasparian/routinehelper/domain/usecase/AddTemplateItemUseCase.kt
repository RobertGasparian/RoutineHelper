package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class AddTemplateItemUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
    ): Long {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return 0

        return routineTemplateRepository.addTemplateItem(
            title = normalizedTitle,
            description = description?.trim()?.takeIf(String::isNotEmpty),
        )
    }
}
