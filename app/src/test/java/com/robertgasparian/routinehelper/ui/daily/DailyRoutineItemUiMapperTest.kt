package com.robertgasparian.routinehelper.ui.daily

import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyRoutineItemUiMapperTest {
    @Test
    fun `given checked repeated hidden today item with note when mapped then preserves tracking ui fields`() {
        val item = TodayRoutineItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            description = "Drink 3L water",
            position = 4,
            date = "2026-05-29",
            isChecked = true,
            isHidden = true,
            note = "One liter was diet soda.",
            repeatTargetCount = 5,
            completedCount = 3,
        )

        val uiState = item.toRoutineTrackingItemUiState()

        assertEquals(
            RoutineTrackingItemUiState(
                routineItemId = 10L,
                actionId = 100L,
                title = "Drink water",
                description = "Drink 3L water",
                repeatTargetCount = 5,
                completedCount = 3,
                isChecked = true,
                isHidden = true,
                note = "One liter was diet soda.",
            ),
            uiState,
        )
    }

    @Test
    fun `given today item note is null when mapped then uses empty note`() {
        val item = TodayRoutineItem(
            routineItemId = 11L,
            actionId = 101L,
            title = "Stretch",
            description = null,
            position = 5,
            date = "2026-05-29",
            isChecked = false,
            note = null,
        )

        val uiState = item.toRoutineTrackingItemUiState()

        assertEquals("", uiState.note)
    }
}
