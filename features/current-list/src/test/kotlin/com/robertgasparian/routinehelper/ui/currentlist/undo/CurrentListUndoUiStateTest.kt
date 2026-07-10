package com.robertgasparian.routinehelper.ui.currentlist.undo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentListUndoUiStateTest {
    @Test
    fun `given no pending items then state is hidden with empty message`() {
        val state = CurrentListUndoUiState()

        assertFalse(state.isVisible)
        assertEquals("", state.message)
    }

    @Test
    fun `given one pending item then state uses singular message`() {
        val state = CurrentListUndoUiState(pendingItemCount = 1)

        assertTrue(state.isVisible)
        assertEquals("1 item removed", state.message)
    }

    @Test
    fun `given multiple pending items then state uses plural message`() {
        val state = CurrentListUndoUiState(pendingItemCount = 3)

        assertTrue(state.isVisible)
        assertEquals("3 items removed", state.message)
    }
}
