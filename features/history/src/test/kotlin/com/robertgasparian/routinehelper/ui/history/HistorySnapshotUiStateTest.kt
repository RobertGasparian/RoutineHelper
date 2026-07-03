package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySnapshotUiStateTest {
    @Test
    fun `given empty snapshot when reading completion state then no actions label and incomplete state are returned`() {
        val state = snapshot(completedCount = 0, totalCount = 0)

        assertEquals("No actions saved", state.completionLabel)
        assertFalse(state.isComplete)
    }

    @Test
    fun `given fully completed snapshot when reading completion state then completed label and complete state are returned`() {
        val state = snapshot(completedCount = 3, totalCount = 3)

        assertEquals("All completed!", state.completionLabel)
        assertTrue(state.isComplete)
    }

    @Test
    fun `given partially completed snapshot when reading completion state then progress label and incomplete state are returned`() {
        val state = snapshot(completedCount = 2, totalCount = 3)

        assertEquals("2/3 completed", state.completionLabel)
        assertFalse(state.isComplete)
    }

    private fun snapshot(
        completedCount: Int,
        totalCount: Int,
    ): HistorySnapshotUiState =
        HistorySnapshotUiState(
            snapshotId = 1L,
            date = "2026-05-29",
            cadence = RoutineCadence.Daily,
            completedCount = completedCount,
            totalCount = totalCount,
            hasSummaryNote = false,
            isSelected = false,
        )
}
