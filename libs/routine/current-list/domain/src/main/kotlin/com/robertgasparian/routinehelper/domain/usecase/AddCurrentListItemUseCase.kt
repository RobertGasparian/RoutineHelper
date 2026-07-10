package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class AddCurrentListItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
    ): Long? {
        val normalizedTitle = normalizeCurrentListTitle(title) ?: return null
        return currentListRepository.addItem(
            title = normalizedTitle,
            description = normalizeCurrentListDescription(description),
        )
    }
}
