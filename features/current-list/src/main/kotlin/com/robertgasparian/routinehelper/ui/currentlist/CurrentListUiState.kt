package com.robertgasparian.routinehelper.ui.currentlist

data class CurrentListUiState(
    val items: List<CurrentListItemUiState> = emptyList(),
    val shareText: String = "",
    val canRemoveItems: Boolean = true,
) {
    val hasItems: Boolean = items.isNotEmpty()
    val canShare: Boolean = shareText.isNotBlank() && hasItems
    val canShowBulkActions: Boolean = items.size >= 2
    val canCheckAll: Boolean = items.any { item -> !item.isChecked }
    val canUncheckAll: Boolean = items.any(CurrentListItemUiState::isChecked)

    companion object {
        fun preview(): CurrentListUiState =
            CurrentListUiState(
                items = listOf(
                    CurrentListItemUiState(
                        id = 1L,
                        title = "Pick up dry cleaning",
                        description = "Before 6 PM",
                        isChecked = false,
                    ),
                    CurrentListItemUiState(
                        id = 2L,
                        title = "Order replacement filters",
                        description = null,
                        isChecked = true,
                    ),
                    CurrentListItemUiState(
                        id = 3L,
                        title = "Send photos to Sam",
                        description = "Use the shared album.",
                        isChecked = false,
                    ),
                ),
                shareText = """
                    Current list

                    1. [ ] Pick up dry cleaning
                       Description: Before 6 PM

                    2. [x] Order replacement filters

                    3. [ ] Send photos to Sam
                       Description: Use the shared album.
                """.trimIndent(),
            )

        fun previewEmpty(): CurrentListUiState =
            CurrentListUiState()
    }
}
