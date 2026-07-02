package com.robertgasparian.routinehelper.ui.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineReorderStateTest {
    @Test
    fun `given unchanged drag order when drag ends then returns null and resets drag state`() {
        val sourceItems = listOf(routineItem(id = 10L), routineItem(id = 11L))
        val reorderState = RoutineReorderState()
        reorderState.syncFromSource(sourceItems)

        reorderState.onDragStart(itemId = 10L)
        val orderedIds = reorderState.onDragEnd()

        assertNull(orderedIds)
        assertEquals(sourceItems, reorderState.displayedItems)
        assertNull(reorderState.draggedItemId)
        assertEquals(0f, reorderState.draggedItemOffset)
    }

    @Test
    fun `given reordered drag order when drag ends then returns ids in displayed order`() {
        val sourceItems = listOf(routineItem(id = 10L), routineItem(id = 11L))
        val reorderState = RoutineReorderState()
        reorderState.syncFromSource(sourceItems)

        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1, offsetAdjustment = 24f)
        val orderedIds = reorderState.onDragEnd()

        assertEquals(listOf(11L, 10L), orderedIds)
        assertEquals(listOf(11L, 10L), reorderState.displayedItems.map(RoutineTrackingItemUiState::routineItemId))
        assertNull(reorderState.draggedItemId)
        assertEquals(0f, reorderState.draggedItemOffset)
    }

    @Test
    fun `given source content changes during drag when drag is cancelled then resets to latest source`() {
        val sourceItems = listOf(routineItem(id = 10L), routineItem(id = 11L))
        val latestSourceItems = listOf(
            routineItem(id = 10L, title = "Updated water"),
            routineItem(id = 11L),
        )
        val reorderState = RoutineReorderState()
        reorderState.syncFromSource(sourceItems)

        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1, offsetAdjustment = 24f)
        reorderState.syncFromSource(latestSourceItems)
        reorderState.onDragCancel()

        assertEquals(latestSourceItems, reorderState.displayedItems)
        assertNull(reorderState.draggedItemId)
        assertEquals(0f, reorderState.draggedItemOffset)
    }

    private fun routineItem(
        id: Long,
        title: String = "Drink water",
    ): RoutineTrackingItemUiState =
        RoutineTrackingItemUiState(
            routineItemId = id,
            actionId = id + 100L,
            title = title,
            description = "Drink 3L water",
            repeatTargetCount = null,
            completedCount = 0,
            isChecked = false,
            note = "",
        )
}
