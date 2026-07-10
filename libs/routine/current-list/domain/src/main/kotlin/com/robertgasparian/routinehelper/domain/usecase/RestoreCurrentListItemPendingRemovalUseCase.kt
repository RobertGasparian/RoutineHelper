package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class RestoreCurrentListItemPendingRemovalUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(itemId: Long) {
        currentListRepository.restorePendingRemoval(itemId)
    }
}
