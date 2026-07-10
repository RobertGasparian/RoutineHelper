package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class SetCurrentListItemCheckedUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(
        itemId: Long,
        isChecked: Boolean,
    ) {
        currentListRepository.setChecked(
            itemId = itemId,
            isChecked = isChecked,
        )
    }
}
