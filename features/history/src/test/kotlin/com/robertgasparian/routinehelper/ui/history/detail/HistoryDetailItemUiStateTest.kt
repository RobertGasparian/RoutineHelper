package com.robertgasparian.routinehelper.ui.history.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDetailItemUiStateTest {
    @Test
    fun `given checked single action when checking completion then item is complete`() {
        assertTrue(item(isChecked = true).isComplete)
    }

    @Test
    fun `given unchecked single action when checking completion then item is incomplete`() {
        assertFalse(item(isChecked = false).isComplete)
    }

    @Test
    fun `given repeat action at target count when checking completion then item is complete`() {
        assertTrue(
            item(
                repeatTargetCount = 3,
                completedCount = 3,
                isChecked = false,
            ).isComplete,
        )
    }

    @Test
    fun `given repeat action below target count when checking completion then item is incomplete`() {
        assertFalse(
            item(
                repeatTargetCount = 3,
                completedCount = 2,
                isChecked = true,
            ).isComplete,
        )
    }

    @Test
    fun `given hidden completed action when checking completion then item is incomplete`() {
        assertFalse(
            item(
                repeatTargetCount = 3,
                completedCount = 3,
                isChecked = true,
                isHidden = true,
            ).isComplete,
        )
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
