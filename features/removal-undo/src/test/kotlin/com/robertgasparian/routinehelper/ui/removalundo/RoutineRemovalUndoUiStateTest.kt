package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineRemovalUndoUiStateTest {
    @Test
    fun `given daily group then state exposes source count and visibility`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.Daily,
            pendingItemCount = 2,
        )

        assertEquals(RoutineRemovalSource.Daily, state.activeSource)
        assertEquals(2, state.pendingItemCount)
        assertEquals(true, state.isVisible)
    }

    @Test
    fun `given one weekly removal then state remains visible`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.Weekly,
            pendingItemCount = 1,
        )

        assertEquals(RoutineRemovalSource.Weekly, state.activeSource)
        assertEquals(1, state.pendingItemCount)
        assertEquals(true, state.isVisible)
    }

    @Test
    fun `given current list group then state identifies current list`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.CurrentList,
            pendingItemCount = 1,
        )

        assertEquals(RoutineRemovalSource.CurrentList, state.activeSource)
        assertEquals(1, state.pendingItemCount)
    }
}
