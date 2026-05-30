package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import kotlinx.coroutines.flow.Flow

interface RoutineTemplateRepository {
    fun templateItems(cadence: RoutineCadence = RoutineCadence.Daily): Flow<List<RoutineTemplateItem>>

    fun templateItem(actionId: Long): Flow<RoutineTemplateItem?>

    suspend fun addTemplateItem(
        title: String,
        description: String?,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ): Long

    suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
    )

    suspend fun removeTemplateItem(routineItemId: Long)

    suspend fun reorderTemplateItems(routineItemIdsInOrder: List<Long>)
}
