package com.robertgasparian.routinehelper.domain.order

import javax.inject.Inject

class RoutineTemplateOrderPlanner @Inject constructor() {
    fun planReorder(
        allItems: List<RoutineTemplateOrderItem>,
        visibleItemIdsInOrder: List<Long>,
    ): List<RoutineTemplatePositionUpdate> {
        val orderedItems = allItems.sortedWith(
            compareBy(RoutineTemplateOrderItem::position, RoutineTemplateOrderItem::id),
        )
        val visibleItemsById = orderedItems
            .filterNot(RoutineTemplateOrderItem::isPendingRemoval)
            .associateBy(RoutineTemplateOrderItem::id)
        val requestedVisibleItems = visibleItemIdsInOrder.mapNotNull(visibleItemsById::get)
        val requestedItemIds = requestedVisibleItems.map(RoutineTemplateOrderItem::id).toSet()
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
                RoutineTemplatePositionUpdate(
                    itemId = item.id,
                    position = index,
                )
            }
        }
    }
}

data class RoutineTemplateOrderItem(
    val id: Long,
    val position: Int,
    val isPendingRemoval: Boolean,
)

data class RoutineTemplatePositionUpdate(
    val itemId: Long,
    val position: Int,
)
