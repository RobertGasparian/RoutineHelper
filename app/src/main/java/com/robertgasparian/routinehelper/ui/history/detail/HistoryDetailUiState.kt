package com.robertgasparian.routinehelper.ui.history.detail

data class HistoryDetailUiState(
    val date: String = "",
    val finalizedLabel: String = "",
    val items: List<HistoryDetailItemUiState> = emptyList(),
    val isMissing: Boolean = false,
    val shareText: String? = null,
) {
    companion object {
        fun preview(): HistoryDetailUiState =
            HistoryDetailUiState(
                date = "2026-05-29",
                finalizedLabel = "Finalized 11:45 PM",
                items = listOf(
                    HistoryDetailItemUiState(
                        actionId = 100,
                        title = "Drink water",
                        description = "Drink 3L water",
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    HistoryDetailItemUiState(
                        actionId = 101,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        isChecked = false,
                        note = null,
                    ),
                ),
            )

        fun previewEmpty(): HistoryDetailUiState =
            HistoryDetailUiState(
                date = "2026-05-29",
                finalizedLabel = "Finalized 11:45 PM",
            )

        fun previewShare(): HistoryDetailUiState =
            preview().copy(
                shareText = """
                    Routine snapshot
                    Date: 2026-05-29
                    Finalized: 11:45 PM

                    1. [x] Drink water
                       Description: Drink 3L water
                       Note: One liter was diet soda.

                    2. [ ] Stretch
                       Description: Ten minutes of mobility work
                """.trimIndent(),
            )

        fun previewMissing(): HistoryDetailUiState =
            HistoryDetailUiState(isMissing = true)
    }
}

data class HistoryDetailItemUiState(
    val actionId: Long,
    val title: String,
    val description: String?,
    val isChecked: Boolean,
    val note: String?,
)
