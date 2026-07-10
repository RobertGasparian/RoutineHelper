package com.robertgasparian.routinehelper.ui.currentlist.undo

data class CurrentListUndoUiState(
    val pendingItemCount: Int = 0,
) {
    val isVisible: Boolean = pendingItemCount > 0
    val message: String = when (pendingItemCount) {
        0 -> ""
        1 -> "1 item removed"
        else -> "$pendingItemCount items removed"
    }

    companion object {
        fun preview(): CurrentListUndoUiState =
            CurrentListUndoUiState(pendingItemCount = 2)
    }
}
