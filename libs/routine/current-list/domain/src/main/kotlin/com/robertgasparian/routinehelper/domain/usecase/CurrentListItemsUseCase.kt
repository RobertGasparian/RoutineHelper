package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CurrentListItemsUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    operator fun invoke(): Flow<List<CurrentListItem>> =
        currentListRepository.currentListItems()
}
