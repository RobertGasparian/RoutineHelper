package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineRemovalUndoUiStateTest {
    @Test
    fun `given daily group when reading message then identifies source and count`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.Daily,
            pendingItemCount = 2,
        )

        assertEquals("2 Daily actions removed", state.message)
    }

    @Test
    fun `given one weekly removal when reading message then uses singular action`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.Weekly,
            pendingItemCount = 1,
        )

        assertEquals("1 Weekly action removed", state.message)
    }

    @Test
    fun `given current list group when reading message then identifies current list`() {
        val state = RoutineRemovalUndoUiState(
            activeSource = RoutineRemovalSource.CurrentList,
            pendingItemCount = 1,
        )

        assertEquals("1 Current List item removed", state.message)
    }
}
