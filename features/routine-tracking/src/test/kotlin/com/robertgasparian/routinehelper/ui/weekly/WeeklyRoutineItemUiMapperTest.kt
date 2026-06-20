package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyRoutineItemUiMapperTest {
    @Test
    fun `given checked repeated hidden weekly item with note when mapped then preserves tracking ui fields`() {
        val item = WeeklyRoutineItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Plan workouts",
            description = "Choose next week's sessions",
            position = 2,
            weekStartDate = "2026-05-25",
            isChecked = true,
            isHidden = true,
            note = "Moved long run to Sunday.",
            repeatTargetCount = 4,
            completedCount = 2,
        )

        val uiState = item.toRoutineTrackingItemUiState()

        assertEquals(
            RoutineTrackingItemUiState(
                routineItemId = 20L,
                actionId = 200L,
                title = "Plan workouts",
                description = "Choose next week's sessions",
                repeatTargetCount = 4,
                completedCount = 2,
                isChecked = true,
                isHidden = true,
                note = "Moved long run to Sunday.",
            ),
            uiState,
        )
    }

    @Test
    fun `given weekly item note is null when mapped then uses empty note`() {
        val item = WeeklyRoutineItem(
            routineItemId = 21L,
            actionId = 201L,
            title = "Meal prep",
            description = null,
            position = 3,
            weekStartDate = "2026-05-25",
            isChecked = false,
            note = null,
        )

        val uiState = item.toRoutineTrackingItemUiState()

        assertEquals("", uiState.note)
    }
}
