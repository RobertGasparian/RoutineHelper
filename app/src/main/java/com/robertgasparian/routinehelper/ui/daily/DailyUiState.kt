package com.robertgasparian.routinehelper.ui.daily

data class DailyUiState(
    val date: String,
    val summaryNote: String = "",
    val items: List<DailyItemUiState> = emptyList(),
) {
    companion object {
        fun preview(): DailyUiState =
            DailyUiState(
                date = "2026-05-29",
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    DailyItemUiState(
                        routineItemId = 1,
                        actionId = 101,
                        title = "Drink water",
                        description = "Drink 3L water",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    DailyItemUiState(
                        routineItemId = 2,
                        actionId = 102,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        note = "",
                    ),
                    DailyItemUiState(
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

        fun previewEmpty(): DailyUiState =
            DailyUiState(date = "2026-05-29")
    }
}

data class DailyItemUiState(
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
