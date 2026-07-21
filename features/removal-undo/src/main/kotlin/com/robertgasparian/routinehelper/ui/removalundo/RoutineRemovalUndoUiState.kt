package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource

data class RoutineRemovalUndoUiState(
    val activeSource: RoutineRemovalSource? = null,
    val pendingItemCount: Int = 0,
) {
    val isVisible: Boolean = activeSource != null && pendingItemCount > 0

    companion object {
        fun preview(): RoutineRemovalUndoUiState =
            RoutineRemovalUndoUiState(
                activeSource = RoutineRemovalSource.Daily,
                pendingItemCount = 2,
            )
    }
}
