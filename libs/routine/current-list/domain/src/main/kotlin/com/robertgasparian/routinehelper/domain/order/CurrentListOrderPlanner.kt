package com.robertgasparian.routinehelper.domain.order

import javax.inject.Inject

class CurrentListOrderPlanner @Inject constructor() {
    fun planReorder(
        allItems: List<CurrentListOrderItem>,
        visibleItemIdsInOrder: List<Long>,
    ): List<CurrentListPositionUpdate> {
        val orderedItems = allItems.sortedWith(compareBy(CurrentListOrderItem::position, CurrentListOrderItem::id))
        val visibleItemsById = orderedItems
            .filterNot(CurrentListOrderItem::isPendingRemoval)
            .associateBy(CurrentListOrderItem::id)
        val requestedVisibleItems = visibleItemIdsInOrder.mapNotNull(visibleItemsById::get)
        val requestedItemIds = requestedVisibleItems.map(CurrentListOrderItem::id).toSet()
        val remainingVisibleItems = orderedItems.filter { item ->
            !item.isPendingRemoval && item.id !in requestedItemIds
        }
        val reorderedVisibleItems = requestedVisibleItems + remainingVisibleItems
        val visibleIterator = reorderedVisibleItems.iterator()
        val reorderedItems = orderedItems.map { item ->
            if (!item.isPendingRemoval && visibleIterator.hasNext()) {
                visibleIterator.next()
            } else {
                item
            }
        }

        return reorderedItems.mapIndexedNotNull { index, item ->
            if (item.position == index) {
                null
            } else {
                CurrentListPositionUpdate(
                    itemId = item.id,
                    position = index,
                )
            }
        }
    }
}

data class CurrentListOrderItem(
    val id: Long,
    val position: Int,
    val isPendingRemoval: Boolean,
)

data class CurrentListPositionUpdate(
    val itemId: Long,
    val position: Int,
)
