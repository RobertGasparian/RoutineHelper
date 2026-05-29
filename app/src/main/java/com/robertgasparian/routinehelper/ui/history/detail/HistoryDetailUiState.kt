package com.robertgasparian.routinehelper.ui.history.detail

data class HistoryDetailUiState(
    val date: String = "",
    val finalizedLabel: String = "",
    val items: List<HistoryDetailItemUiState> = emptyList(),
    val isMissing: Boolean = false,
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
