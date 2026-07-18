package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource

data class RoutineRemovalUndoUiState(
    val activeSource: RoutineRemovalSource? = null,
    val pendingItemCount: Int = 0,
) {
    val isVisible: Boolean = activeSource != null && pendingItemCount > 0

    val message: String = when (activeSource) {
        RoutineRemovalSource.CurrentList -> itemMessage(
            singular = "Current List item",
            plural = "Current List items",
        )
        RoutineRemovalSource.Daily -> itemMessage(
            singular = "Daily action",
            plural = "Daily actions",
        )
        RoutineRemovalSource.Weekly -> itemMessage(
            singular = "Weekly action",
            plural = "Weekly actions",
        )
        null -> ""
    }

    private fun itemMessage(
        singular: String,
        plural: String,
    ): String = when (pendingItemCount) {
        0 -> ""
        1 -> "1 $singular removed"
        else -> "$pendingItemCount $plural removed"
    }

    companion object {
        fun preview(): RoutineRemovalUndoUiState =
            RoutineRemovalUndoUiState(
                activeSource = RoutineRemovalSource.Daily,
                pendingItemCount = 2,
            )
    }
}
