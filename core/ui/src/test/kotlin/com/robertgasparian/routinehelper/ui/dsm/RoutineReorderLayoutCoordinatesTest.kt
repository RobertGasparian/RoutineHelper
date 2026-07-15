package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineReorderLayoutCoordinatesTest {
    @Test
    fun `given rendered item position when recorded then bounds use container coordinates`() {
        val layoutCoordinates = RoutineReorderLayoutCoordinates().apply {
            containerTopInRoot = 64f
        }

        val itemBounds = layoutCoordinates.onItemPlaced(
            itemId = 10L,
            itemTopInRoot = 184f,
            itemSize = 80,
        )

        assertEquals(RoutineReorderItemBounds(top = 120f, size = 80), itemBounds)
        assertEquals(itemBounds, layoutCoordinates.itemBounds(itemId = 10L))
    }

    @Test
    fun `given disposed item when bounds are requested then returns null`() {
        val layoutCoordinates = RoutineReorderLayoutCoordinates()
        layoutCoordinates.onItemPlaced(
            itemId = 10L,
            itemTopInRoot = 120f,
            itemSize = 80,
        )

        layoutCoordinates.onItemDisposed(itemId = 10L)

        assertNull(layoutCoordinates.itemBounds(itemId = 10L))
    }
}
