package com.robertgasparian.routinehelper.ui.currentlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrentListReorderStateTest {
    @Test
    fun `given unchanged drag order when drag ends then returns null and resets drag state`() {
        val sourceItems = listOf(currentListItem(id = 10L), currentListItem(id = 11L))
        val reorderState = CurrentListReorderState()
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
        val sourceItems = listOf(currentListItem(id = 10L), currentListItem(id = 11L))
        val reorderState = CurrentListReorderState()
        reorderState.syncFromSource(sourceItems)

        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1, offsetAdjustment = 24f)
        val orderedIds = reorderState.onDragEnd()

        assertEquals(listOf(11L, 10L), orderedIds)
        assertEquals(listOf(11L, 10L), reorderState.displayedItems.map(CurrentListItemUiState::id))
        assertNull(reorderState.draggedItemId)
        assertEquals(0f, reorderState.draggedItemOffset)
    }

    @Test
    fun `given saving reorder when source removes an item then drops it and recognizes surviving persisted order`() {
        val reorderState = CurrentListReorderState()
        reorderState.syncFromSource(
            listOf(
                currentListItem(id = 10L),
                currentListItem(id = 11L),
                currentListItem(id = 12L),
            ),
        )
        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1, offsetAdjustment = 24f)
        reorderState.onDragEnd()

        reorderState.syncFromSource(
            listOf(
                currentListItem(id = 10L),
                currentListItem(id = 11L),
            ),
        )

        assertEquals(listOf(11L, 10L), reorderState.displayedItems.map(CurrentListItemUiState::id))

        reorderState.syncFromSource(
            listOf(
                currentListItem(id = 11L),
                currentListItem(id = 10L),
            ),
        )
        reorderState.syncFromSource(
            listOf(
                currentListItem(id = 10L),
                currentListItem(id = 11L),
            ),
        )

        assertEquals(listOf(10L, 11L), reorderState.displayedItems.map(CurrentListItemUiState::id))
    }

    private fun currentListItem(id: Long): CurrentListItemUiState =
        CurrentListItemUiState(
            id = id,
            title = "Task",
            description = null,
            isChecked = false,
        )
}
