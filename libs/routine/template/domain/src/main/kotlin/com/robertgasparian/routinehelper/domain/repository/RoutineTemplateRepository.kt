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
        repeatTargetCount: Int?,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ): Long

    suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
        repeatTargetCount: Int?,
    )

    suspend fun removeTemplateItem(routineItemId: Long)

    suspend fun markPendingRemoval(
        cadence: RoutineCadence,
        routineItemId: Long,
    )

    suspend fun restorePendingRemoval(
        cadence: RoutineCadence,
        routineItemId: Long,
    )

    suspend fun restorePendingRemovals(
        cadence: RoutineCadence,
        routineItemIds: List<Long>,
    )

    suspend fun deletePendingRemovals(
        cadence: RoutineCadence,
        routineItemIds: List<Long>,
    )

    suspend fun deleteAllPendingRemovals()

    suspend fun reorderTemplateItems(
        cadence: RoutineCadence,
        routineItemIdsInOrder: List<Long>,
    )
}
