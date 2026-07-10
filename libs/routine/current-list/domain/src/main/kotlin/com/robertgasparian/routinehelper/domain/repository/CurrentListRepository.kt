package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import kotlinx.coroutines.flow.Flow

interface CurrentListRepository {
    fun currentListItems(): Flow<List<CurrentListItem>>

    suspend fun addItem(
        title: String,
        description: String?,
    ): Long

    suspend fun setChecked(
        itemId: Long,
        isChecked: Boolean,
    )

    suspend fun setAllChecked(isChecked: Boolean)

    suspend fun markPendingRemoval(itemId: Long)

    suspend fun restorePendingRemoval(itemId: Long)

    suspend fun restorePendingRemovals(itemIds: List<Long>)

    suspend fun deletePendingRemovals(itemIds: List<Long>)

    suspend fun deleteAllPendingRemovals()

    suspend fun reorderItems(itemIdsInOrder: List<Long>)

    suspend fun clearItems()
}
