package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class RoutineReorderState<Item>(
    initialItems: List<Item> = emptyList(),
) {
    var displayedItems by mutableStateOf(initialItems)
        private set
    var pressedHandleItemId by mutableStateOf<Long?>(null)
        private set
    var draggedItemId by mutableStateOf<Long?>(null)
        private set
    var draggedItemTop by mutableStateOf<Float?>(null)
        private set
    var draggedItemSize: Int = 0
        private set
    var draggedItemSlotTop: Float? = null
        private set
    var isContainerDragActive: Boolean = false
        private set
    var isDropAnimating by mutableStateOf(false)
        private set
    private var dragDeltaBeforeContainer: Float = 0f
    private var sourceItems: List<Item> = initialItems
    private var phase: RoutineReorderPhase = RoutineReorderPhase.Idle

    fun syncFromSource(
        items: List<Item>,
        itemId: (Item) -> Long,
    ) {
        if (items == sourceItems) return

        sourceItems = items
        phase = when (val currentPhase = phase) {
            RoutineReorderPhase.Idle -> {
                displayedItems = items
                RoutineReorderPhase.Idle
            }
            RoutineReorderPhase.Dragging -> {
                displayedItems = displayedItems.mergeItemContent(items, itemId)
                currentPhase
            }
            is RoutineReorderPhase.AwaitingSourceConfirmation -> {
                if (items.hasPersistedOrder(currentPhase.orderedIds, itemId)) {
                    displayedItems = items
                    RoutineReorderPhase.Idle
                } else {
                    displayedItems = displayedItems.mergeItemContent(items, itemId)
                    currentPhase
                }
            }
        }
    }

    fun onHandlePress(itemId: Long) {
        pressedHandleItemId = itemId
    }

    fun onHandleRelease(itemId: Long) {
        if (pressedHandleItemId == itemId) {
            pressedHandleItemId = null
        }
    }

    fun onDragStart(itemId: Long) {
        if (isDropAnimating) return

        pressedHandleItemId = itemId
        draggedItemId = itemId
        draggedItemSize = 0
        draggedItemTop = null
        draggedItemSlotTop = null
        dragDeltaBeforeContainer = 0f
        isContainerDragActive = false
        phase = RoutineReorderPhase.Dragging
    }

    fun onContainerDragStart(
        itemTop: Float,
        itemSize: Int,
    ) {
        draggedItemSize = itemSize
        draggedItemTop = itemTop + dragDeltaBeforeContainer
        dragDeltaBeforeContainer = 0f
        isContainerDragActive = true
    }

    fun onDrag(deltaY: Float) {
        val currentTop = draggedItemTop
        if (currentTop == null) {
            dragDeltaBeforeContainer += deltaY
        } else {
            draggedItemTop = currentTop + deltaY
        }
    }

    fun onDropAnimationFrame(itemTop: Float) {
        if (draggedItemId != null) {
            draggedItemTop = itemTop
        }
    }

    fun onDraggedItemSlotPlaced(itemTop: Float) {
        if (draggedItemId != null) {
            draggedItemSlotTop = itemTop
        }
    }

    fun onDropAnimationStart() {
        if (draggedItemId != null && draggedItemTop != null) {
            pressedHandleItemId = null
            isContainerDragActive = false
            isDropAnimating = true
        }
    }

    fun move(
        fromIndex: Int,
        toIndex: Int,
    ) {
        displayedItems = displayedItems.moveItem(fromIndex, toIndex)
    }

    fun onDragCancel() {
        clearActiveDrag()
        displayedItems = sourceItems
        phase = RoutineReorderPhase.Idle
    }

    fun onDragEnd(itemId: (Item) -> Long): List<Long>? {
        val orderedIds = displayedItems.map(itemId)
        val sourceIds = sourceItems.map(itemId)
        clearActiveDrag()
        if (orderedIds == sourceIds) {
            displayedItems = sourceItems
            phase = RoutineReorderPhase.Idle
            return null
        }
        phase = RoutineReorderPhase.AwaitingSourceConfirmation(orderedIds)
        return orderedIds
    }

    private fun clearActiveDrag() {
        pressedHandleItemId = null
        draggedItemId = null
        draggedItemSize = 0
        draggedItemTop = null
        draggedItemSlotTop = null
        dragDeltaBeforeContainer = 0f
        isContainerDragActive = false
        isDropAnimating = false
    }
}

private sealed interface RoutineReorderPhase {
    data object Idle : RoutineReorderPhase
    data object Dragging : RoutineReorderPhase

    data class AwaitingSourceConfirmation(
        val orderedIds: List<Long>,
    ) : RoutineReorderPhase
}

private fun <Item> List<Item>.moveItem(
    fromIndex: Int,
    toIndex: Int,
): List<Item> =
    toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }

private fun <Item> List<Item>.mergeItemContent(
    sourceItems: List<Item>,
    itemId: (Item) -> Long,
): List<Item> {
    val sourceById = sourceItems.associateBy(itemId)
    val orderedIds = map(itemId).toSet()
    val updatedOrderedItems = mapNotNull { item -> sourceById[itemId(item)] }
    val newItems = sourceItems.filterNot { item -> itemId(item) in orderedIds }

    return updatedOrderedItems + newItems
}

private fun <Item> List<Item>.hasPersistedOrder(
    expectedIds: List<Long>,
    itemId: (Item) -> Long,
): Boolean {
    val sourceIds = map(itemId)
    val sourceIdSet = sourceIds.toSet()
    val retainedExpectedIds = expectedIds.filter(sourceIdSet::contains)
    val retainedExpectedIdSet = retainedExpectedIds.toSet()
    return sourceIds.filter(retainedExpectedIdSet::contains) == retainedExpectedIds
}
