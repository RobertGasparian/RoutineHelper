package com.robertgasparian.routinehelper.ui.tracking

data class RoutineTrackingUiState(
    val date: String,
    val summaryNote: String = "",
    val items: List<RoutineTrackingItemUiState> = emptyList(),
    val noteEditor: NoteEditorUiState? = null,
) {
    companion object {
        fun preview(): RoutineTrackingUiState =
            RoutineTrackingUiState(
                date = "2026-05-29",
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineTrackingItemUiState(
                        routineItemId = 1,
                        actionId = 101,
                        title = "Drink water",
                        description = "Drink 3L water",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 2,
                        actionId = 102,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        note = "",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 3,
                        actionId = 103,
                        title = "Read",
                        description = null,
                        repeatTargetCount = 5,
                        completedCount = 2,
                        isChecked = false,
                        note = "Finish the last chapter tonight.",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 4,
                        actionId = 104,
                        title = "Run",
                        description = "Rest day for the knee",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        isHidden = true,
                        note = "Skipped intentionally today.",
                    ),
                ),
            )

        fun previewEmpty(): RoutineTrackingUiState =
            RoutineTrackingUiState(date = "2026-05-29")
    }
}
