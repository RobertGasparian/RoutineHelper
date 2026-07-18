package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class UpdateCurrentListItemUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        itemId: Long,
        title: String,
        description: String?,
    ) {
        val normalizedTitle = normalizeCurrentListTitle(title) ?: return
        currentListRepository.updateItem(
            itemId = itemId,
            title = normalizedTitle,
            description = normalizeCurrentListDescription(description),
        )
    }
}
