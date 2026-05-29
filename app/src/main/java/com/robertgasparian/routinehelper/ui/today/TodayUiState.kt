package com.robertgasparian.routinehelper.ui.today

data class TodayUiState(
    val date: String,
    val items: List<TodayItemUiState> = emptyList(),
) {
    companion object {
        fun preview(): TodayUiState =
            TodayUiState(
                date = "2026-05-29",
                items = listOf(
                    TodayItemUiState(
                        routineItemId = 1,
                        title = "Drink water",
                        description = "Drink 3L water",
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    TodayItemUiState(
                        routineItemId = 2,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        isChecked = false,
                        note = "",
                    ),
                    TodayItemUiState(
                        routineItemId = 3,
                        title = "Read",
                        description = null,
                        isChecked = false,
                        note = "Finish the last chapter tonight.",
                    ),
                ),
            )

        fun previewEmpty(): TodayUiState =
            TodayUiState(date = "2026-05-29")
    }
}

data class TodayItemUiState(
    val routineItemId: Long,
    val title: String,
    val description: String?,
    val isChecked: Boolean,
    val note: String,
)
