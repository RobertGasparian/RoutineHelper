package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineReorderGeometryTest {
    @Test
    fun `given dragged center has not crossed target center then order does not move`() {
        val move = calculateReorderMove(
            itemIds = listOf(10L, 11L),
            itemLayouts = twoItemLayouts(),
            draggedItemId = 10L,
            draggedItemTop = 111f,
            draggedItemSize = 100,
            direction = ReorderDirection.Down,
        )

        assertNull(move)
    }

    @Test
    fun `given dragged center crosses target center then move changes logical order`() {
        val move = calculateReorderMove(
            itemIds = listOf(10L, 11L),
            itemLayouts = twoItemLayouts(),
            draggedItemId = 10L,
            draggedItemTop = 113f,
            draggedItemSize = 100,
            direction = ReorderDirection.Down,
        )

        assertEquals(
            ReorderMove(
                fromIndex = 0,
                toIndex = 1,
            ),
            move,
        )
    }

    @Test
    fun `given dragged placeholder is not visible when center crosses visible neighbor then order still moves`() {
        val move = calculateReorderMove(
            itemIds = listOf(10L, 11L, 12L),
            itemLayouts = listOf(
                ReorderItemLayout(id = 10L, offset = 0, size = 100),
                ReorderItemLayout(id = 11L, offset = 112, size = 100),
            ),
            draggedItemId = 12L,
            draggedItemTop = 0f,
            draggedItemSize = 100,
            direction = ReorderDirection.Up,
        )

        assertEquals(ReorderMove(fromIndex = 2, toIndex = 1), move)
    }

    @Test
    fun `given adjacent target is not visible when farther item is crossed then order does not skip items`() {
        val move = calculateReorderMove(
            itemIds = listOf(10L, 11L, 12L),
            itemLayouts = listOf(
                ReorderItemLayout(id = 10L, offset = 0, size = 100),
                ReorderItemLayout(id = 12L, offset = 112, size = 100),
            ),
            draggedItemId = 10L,
            draggedItemTop = 113f,
            draggedItemSize = 100,
            direction = ReorderDirection.Down,
        )

        assertNull(move)
    }

    @Test
    fun `given tall item reverses direction then only neighbor in active direction is considered`() {
        val itemIds = listOf(10L, 11L, 12L, 13L)
        val itemLayouts = listOf(
            ReorderItemLayout(id = 10L, offset = -224, size = 100),
            ReorderItemLayout(id = 11L, offset = -112, size = 100),
            ReorderItemLayout(id = 13L, offset = 112, size = 100),
        )

        val upwardMove = calculateReorderMove(
            itemIds = itemIds,
            itemLayouts = itemLayouts,
            draggedItemId = 12L,
            draggedItemTop = -300f,
            draggedItemSize = 408,
            direction = ReorderDirection.Up,
        )
        val downwardMove = calculateReorderMove(
            itemIds = itemIds,
            itemLayouts = itemLayouts,
            draggedItemId = 12L,
            draggedItemTop = -300f,
            draggedItemSize = 408,
            direction = ReorderDirection.Down,
        )

        assertEquals(ReorderMove(fromIndex = 2, toIndex = 1), upwardMove)
        assertNull(downwardMove)
    }

    private fun twoItemLayouts(): List<ReorderItemLayout> =
        listOf(
            ReorderItemLayout(id = 10L, offset = 0, size = 100),
            ReorderItemLayout(id = 11L, offset = 112, size = 100),
        )
}
