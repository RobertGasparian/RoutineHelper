package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineReorderStateTest {
    @Test
    fun `given unchanged source when synchronized again then skips reconciliation`() {
        val sourceItems = listOf(item(id = 10L), item(id = 11L))
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(sourceItems, TestItem::id)
        var itemIdInvocationCount = 0

        reorderState.syncFromSource(sourceItems) { item ->
            itemIdInvocationCount += 1
            item.id
        }

        assertEquals(0, itemIdInvocationCount)
    }

    @Test
    fun `given unchanged drag order when drag ends then returns null and resets drag state`() {
        val sourceItems = listOf(item(id = 10L), item(id = 11L))
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(sourceItems, TestItem::id)

        reorderState.onDragStart(itemId = 10L)
        val orderedIds = reorderState.onDragEnd(TestItem::id)

        assertNull(orderedIds)
        assertEquals(sourceItems, reorderState.displayedItems)
        assertNull(reorderState.draggedItemId)
        assertNull(reorderState.draggedItemTop)
        assertEquals(0, reorderState.draggedItemSize)
    }

    @Test
    fun `given reordered drag order when drag ends then returns ids in displayed order`() {
        val sourceItems = listOf(item(id = 10L), item(id = 11L))
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(sourceItems, TestItem::id)

        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1)
        val orderedIds = reorderState.onDragEnd(TestItem::id)

        assertEquals(listOf(11L, 10L), orderedIds)
        assertEquals(listOf(11L, 10L), reorderState.displayedItems.map(TestItem::id))
        assertNull(reorderState.draggedItemId)
        assertNull(reorderState.draggedItemTop)
        assertEquals(0, reorderState.draggedItemSize)
    }

    @Test
    fun `given source content changes during drag when drag is cancelled then resets to latest source`() {
        val sourceItems = listOf(item(id = 10L), item(id = 11L))
        val latestSourceItems = listOf(item(id = 10L, title = "Updated"), item(id = 11L))
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(sourceItems, TestItem::id)

        reorderState.onDragStart(itemId = 10L)
        reorderState.onContainerDragStart(itemTop = 100f, itemSize = 80)
        reorderState.move(fromIndex = 0, toIndex = 1)
        reorderState.syncFromSource(latestSourceItems, TestItem::id)
        reorderState.onDragCancel()

        assertEquals(latestSourceItems, reorderState.displayedItems)
        assertNull(reorderState.pressedHandleItemId)
        assertNull(reorderState.draggedItemId)
        assertNull(reorderState.draggedItemTop)
        assertEquals(0, reorderState.draggedItemSize)
        assertFalse(reorderState.isContainerDragActive)
    }

    @Test
    fun `given reorder awaiting source confirmation when membership changes then reconciles surviving order`() {
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(
            items = listOf(item(id = 10L), item(id = 11L), item(id = 12L)),
            itemId = TestItem::id,
        )
        reorderState.onDragStart(itemId = 10L)
        reorderState.move(fromIndex = 0, toIndex = 1)
        reorderState.onDragEnd(TestItem::id)

        reorderState.syncFromSource(
            items = listOf(item(id = 10L), item(id = 11L), item(id = 13L)),
            itemId = TestItem::id,
        )

        assertEquals(listOf(11L, 10L, 13L), reorderState.displayedItems.map(TestItem::id))

        reorderState.syncFromSource(
            items = listOf(item(id = 11L), item(id = 10L), item(id = 13L)),
            itemId = TestItem::id,
        )
        reorderState.syncFromSource(
            items = listOf(item(id = 10L), item(id = 11L), item(id = 13L)),
            itemId = TestItem::id,
        )

        assertEquals(listOf(10L, 11L, 13L), reorderState.displayedItems.map(TestItem::id))
    }

    @Test
    fun `when handle is pressed and released then active item is exposed`() {
        val reorderState = RoutineReorderState<TestItem>()

        reorderState.onHandlePress(itemId = 10L)
        assertEquals(10L, reorderState.pressedHandleItemId)

        reorderState.onHandleRelease(itemId = 11L)
        assertEquals(10L, reorderState.pressedHandleItemId)

        reorderState.onHandleRelease(itemId = 10L)
        assertNull(reorderState.pressedHandleItemId)
    }

    @Test
    fun `given active drag when item order moves then overlay top remains unchanged`() {
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(listOf(item(10L), item(11L)), TestItem::id)
        reorderState.onDragStart(itemId = 10L)
        reorderState.onContainerDragStart(itemTop = 0f, itemSize = 100)
        reorderState.onDrag(deltaY = 113f)
        reorderState.move(fromIndex = 0, toIndex = 1)

        assertEquals(listOf(11L, 10L), reorderState.displayedItems.map(TestItem::id))
        assertEquals(113f, reorderState.draggedItemTop)
    }

    @Test
    fun `given active drag when drop animation advances then overlay top follows animation`() {
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(listOf(item(10L), item(11L)), TestItem::id)
        reorderState.onDragStart(itemId = 10L)
        reorderState.onContainerDragStart(itemTop = 0f, itemSize = 100)
        reorderState.onDraggedItemSlotPlaced(itemTop = 112f)

        reorderState.onDropAnimationStart()
        reorderState.onDragStart(itemId = 11L)
        reorderState.onDropAnimationFrame(itemTop = 72f)

        assertTrue(reorderState.isDropAnimating)
        assertEquals(10L, reorderState.draggedItemId)
        assertEquals(72f, reorderState.draggedItemTop)
        assertEquals(112f, reorderState.draggedItemSlotTop)

        reorderState.onDropAnimationFrame(itemTop = 100f)
        reorderState.onDragEnd(TestItem::id)

        assertFalse(reorderState.isDropAnimating)
        assertNull(reorderState.draggedItemSlotTop)
    }

    @Test
    fun `given drag begins before container ownership then pending delta is applied to overlay top`() {
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(listOf(item(10L), item(11L)), TestItem::id)
        reorderState.onDragStart(itemId = 10L)
        reorderState.onDrag(deltaY = 40f)

        reorderState.onContainerDragStart(itemTop = 100f, itemSize = 80)
        reorderState.onDrag(deltaY = 20f)
        reorderState.move(fromIndex = 0, toIndex = 1)

        assertEquals(160f, reorderState.draggedItemTop)
        assertEquals(80, reorderState.draggedItemSize)
    }

    @Test
    fun `given drag ownership transfers to container when drag ends then container ownership resets`() {
        val reorderState = RoutineReorderState<TestItem>()
        reorderState.syncFromSource(listOf(item(10L), item(11L)), TestItem::id)
        reorderState.onDragStart(itemId = 10L)

        reorderState.onContainerDragStart(itemTop = 0f, itemSize = 100)

        assertTrue(reorderState.isContainerDragActive)

        reorderState.onDragEnd(TestItem::id)

        assertFalse(reorderState.isContainerDragActive)
    }

    private fun item(
        id: Long,
        title: String = "Task",
    ): TestItem = TestItem(id = id, title = title)
}

private data class TestItem(
    val id: Long,
    val title: String,
)
