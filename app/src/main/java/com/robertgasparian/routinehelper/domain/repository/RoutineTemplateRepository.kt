package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import kotlinx.coroutines.flow.Flow

interface RoutineTemplateRepository {
    fun templateItems(): Flow<List<RoutineTemplateItem>>

    suspend fun addTemplateItem(
        title: String,
        description: String?,
    ): Long

    suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
    )

    suspend fun removeTemplateItem(routineItemId: Long)

    suspend fun reorderTemplateItems(routineItemIdsInOrder: List<Long>)
}
