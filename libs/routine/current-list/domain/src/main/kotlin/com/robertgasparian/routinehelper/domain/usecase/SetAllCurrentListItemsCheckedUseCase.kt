package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject

class SetAllCurrentListItemsCheckedUseCase @Inject constructor(
    private val currentListRepository: CurrentListRepository,
) {
    suspend operator fun invoke(isChecked: Boolean) {
        currentListRepository.setAllChecked(isChecked)
    }
}
