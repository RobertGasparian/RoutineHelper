package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.ReflectionTagTemplateRepository
import javax.inject.Inject

class ReflectionTagsUseCase @Inject constructor(
    private val repository: ReflectionTagTemplateRepository,
) {
    operator fun invoke(cadence: RoutineCadence) = repository.tags(cadence)
}
