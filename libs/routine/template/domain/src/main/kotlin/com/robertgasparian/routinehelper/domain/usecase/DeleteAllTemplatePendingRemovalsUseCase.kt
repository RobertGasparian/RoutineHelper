package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import javax.inject.Inject

class DeleteAllTemplatePendingRemovalsUseCase @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
) {
    suspend operator fun invoke() {
        routineTemplateRepository.deleteAllPendingRemovals()
    }
}
