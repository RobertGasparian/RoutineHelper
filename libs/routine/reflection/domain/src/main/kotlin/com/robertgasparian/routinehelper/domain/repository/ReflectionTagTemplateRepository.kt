package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import kotlinx.coroutines.flow.Flow

interface ReflectionTagTemplateRepository {
    fun tags(cadence: RoutineCadence): Flow<List<ReflectionTagDefinition>>

    suspend fun reconcile(
        cadence: RoutineCadence,
        originalTagIds: Set<Long>,
        draft: List<ReflectionTagTemplateDraft>,
    ): List<SelectedReflectionTag>
}
