package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrentListRepository : CurrentListRepository {
    private val items = MutableStateFlow<List<CurrentListItem>>(emptyList())
    val addedItems = mutableListOf<AddedCurrentListItem>()
    val checkedChanges = mutableListOf<CurrentListCheckedChange>()
    val allCheckedChanges = mutableListOf<Boolean>()
    val pendingRemovalItemIds = mutableListOf<Long>()
    val restoredPendingRemovalItemIds = mutableListOf<Long>()
    val restoredPendingRemovalItemIdGroups = mutableListOf<List<Long>>()
    val deletedPendingRemovalItemIdGroups = mutableListOf<List<Long>>()
    val reorderedItemIds = mutableListOf<List<Long>>()
    var clearCount = 0
    var deleteAllPendingRemovalsCount = 0

    fun setItems(items: List<CurrentListItem>) {
        this.items.value = items
    }

    override fun currentListItems(): Flow<List<CurrentListItem>> = items

    override suspend fun addItem(
        title: String,
        description: String?,
    ): Long {
        addedItems += AddedCurrentListItem(
            title = title,
            description = description,
        )
        return addedItems.size.toLong()
    }

    override suspend fun setChecked(
        itemId: Long,
        isChecked: Boolean,
    ) {
        checkedChanges += CurrentListCheckedChange(
            itemId = itemId,
            isChecked = isChecked,
        )
    }

    override suspend fun setAllChecked(isChecked: Boolean) {
        allCheckedChanges += isChecked
    }

    override suspend fun markPendingRemoval(itemId: Long) {
        pendingRemovalItemIds += itemId
    }

    override suspend fun restorePendingRemoval(itemId: Long) {
        restoredPendingRemovalItemIds += itemId
    }

    override suspend fun restorePendingRemovals(itemIds: List<Long>) {
        restoredPendingRemovalItemIdGroups += itemIds
    }

    override suspend fun deletePendingRemovals(itemIds: List<Long>) {
        deletedPendingRemovalItemIdGroups += itemIds
    }

    override suspend fun deleteAllPendingRemovals() {
        deleteAllPendingRemovalsCount += 1
    }

    override suspend fun reorderItems(itemIdsInOrder: List<Long>) {
        reorderedItemIds += itemIdsInOrder
    }

    override suspend fun clearItems() {
        clearCount += 1
    }
}

data class AddedCurrentListItem(
    val title: String,
    val description: String?,
)

data class CurrentListCheckedChange(
    val itemId: Long,
    val isChecked: Boolean,
)
