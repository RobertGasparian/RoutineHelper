package com.robertgasparian.routinehelper.ui.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareTextPreviewUiStateTest {
    @Test
    fun `given blank text when state is created then sharing is disabled`() {
        val state = ShareTextPreviewUiState(text = "   ")

        assertFalse(state.canShare)
        assertFalse(state.isOverSoftLimit)
        assertEquals("3 characters", state.characterCountLabel)
    }

    @Test
    fun `given non blank text when state is created then sharing is enabled`() {
        val state = ShareTextPreviewUiState(text = "Routine snapshot")

        assertTrue(state.canShare)
        assertFalse(state.isOverSoftLimit)
        assertEquals("16 characters", state.characterCountLabel)
    }

    @Test
    fun `given text over soft limit when state is created then long message warning is enabled`() {
        val state = ShareTextPreviewUiState(text = "A".repeat(4_001))

        assertTrue(state.isOverSoftLimit)
    }
}
