package com.robertgasparian.routinehelper.ui.history.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDetailUiStateTest {
    @Test
    fun `given no visible items when reading completion summary then empty summary is returned`() {
        val state = HistoryDetailUiState(
            items = listOf(item(isChecked = true, isHidden = true)),
        )

        assertEquals(HistoryDetailCompletionSummary.Empty, state.completionSummary)
        assertEquals("No actions saved", state.completionSummary.label)
    }

    @Test
    fun `given all visible items complete when reading completion summary then all complete summary is returned`() {
        val state = HistoryDetailUiState(
            items = listOf(
                item(isChecked = true),
                item(repeatTargetCount = 3, completedCount = 3),
                item(isChecked = false, isHidden = true),
            ),
        )

        assertEquals(HistoryDetailCompletionSummary.AllComplete, state.completionSummary)
        assertEquals("All completed!", state.completionSummary.label)
    }

    @Test
    fun `given partially complete visible items when reading completion summary then partial summary is returned`() {
        val state = HistoryDetailUiState(
            items = listOf(
                item(isChecked = true),
                item(repeatTargetCount = 3, completedCount = 2),
            ),
        )

        assertEquals(
            HistoryDetailCompletionSummary.Partial(completedCount = 1, totalCount = 2),
            state.completionSummary,
        )
        assertEquals("1 of 2 completed", state.completionSummary.label)
    }

    private fun item(
        repeatTargetCount: Int? = null,
        completedCount: Int = 0,
        isChecked: Boolean = false,
        isHidden: Boolean = false,
    ): HistoryDetailItemUiState =
        HistoryDetailItemUiState(
            actionId = 1L,
            title = "Morning stretch",
            description = null,
            repeatTargetCount = repeatTargetCount,
            completedCount = completedCount,
            isChecked = isChecked,
            isHidden = isHidden,
            note = null,
        )
}
