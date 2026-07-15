package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineReorderAutoScrollerTest {
    @Test
    fun `given dragged item outside edge zones when calculating auto-scroll then returns zero`() {
        val scrollDelta = calculateReorderAutoScrollDelta(
            draggedStart = 100f,
            draggedEnd = 200f,
            viewportStart = 0,
            viewportEnd = 300,
            edgeThreshold = 64f,
            maxScrollPerFrame = 12f,
        )

        assertEquals(0f, scrollDelta)
    }

    @Test
    fun `given dragged item halfway through top edge zone when calculating auto-scroll then scales upward speed`() {
        val scrollDelta = calculateReorderAutoScrollDelta(
            draggedStart = 32f,
            draggedEnd = 132f,
            viewportStart = 0,
            viewportEnd = 300,
            edgeThreshold = 64f,
            maxScrollPerFrame = 12f,
        )

        assertEquals(-6f, scrollDelta)
    }

    @Test
    fun `given dragged item beyond bottom edge when calculating auto-scroll then clamps downward speed`() {
        val scrollDelta = calculateReorderAutoScrollDelta(
            draggedStart = 250f,
            draggedEnd = 350f,
            viewportStart = 0,
            viewportEnd = 300,
            edgeThreshold = 64f,
            maxScrollPerFrame = 12f,
        )

        assertEquals(12f, scrollDelta)
    }
}
