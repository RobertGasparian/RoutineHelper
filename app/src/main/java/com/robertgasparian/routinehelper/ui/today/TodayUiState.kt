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
                        actionId = 101,
                        title = "Drink water",
                        description = "Drink 3L water",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    TodayItemUiState(
                        routineItemId = 2,
                        actionId = 102,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        note = "",
                    ),
                    TodayItemUiState(
                        routineItemId = 3,
                        actionId = 103,
                        title = "Read",
                        description = null,
                        repeatTargetCount = 5,
                        completedCount = 2,
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
    val actionId: Long,
    val title: String,
    val description: String?,
    val repeatTargetCount: Int?,
    val completedCount: Int,
    val isChecked: Boolean,
    val note: String,
) {
    val isRepeatAction: Boolean = repeatTargetCount != null
}
