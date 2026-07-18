package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RoutineSwipeToRevealTest {
    @Test
    fun `given left-to-right layout when reveal offset is calculated then content moves toward start`() {
        val offset = calculateRoutineSwipeRevealOffset(
            revealWidthPx = 80f,
            layoutDirection = LayoutDirection.Ltr,
        )

        assertEquals(-80f, offset)
    }

    @Test
    fun `given right-to-left layout when reveal offset is calculated then content moves toward start`() {
        val offset = calculateRoutineSwipeRevealOffset(
            revealWidthPx = 80f,
            layoutDirection = LayoutDirection.Rtl,
        )

        assertEquals(80f, offset)
    }

    @Test
    fun `given invalid reveal width when reveal offset is calculated then it fails fast`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculateRoutineSwipeRevealOffset(
                revealWidthPx = 0f,
                layoutDirection = LayoutDirection.Ltr,
            )
        }
    }
}
