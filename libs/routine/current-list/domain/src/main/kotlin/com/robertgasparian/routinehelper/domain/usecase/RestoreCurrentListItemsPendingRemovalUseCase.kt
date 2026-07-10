package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class RestoreCurrentListItemsPendingRemovalUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(itemIds: List<Long>) {
        currentListRepository.restorePendingRemovals(itemIds)
    }
}
