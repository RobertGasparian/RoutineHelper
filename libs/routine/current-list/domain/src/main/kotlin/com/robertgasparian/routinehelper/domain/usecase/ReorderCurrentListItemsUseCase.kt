package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class ReorderCurrentListItemsUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(itemIdsInOrder: List<Long>) {
        currentListRepository.reorderItems(itemIdsInOrder)
    }
}
