package com.robertgasparian.routinehelper.ui.history.detail

sealed interface HistoryDetailCompletionSummary {
    val label: String
    val isComplete: Boolean

    data object Empty : HistoryDetailCompletionSummary {
        override val label: String = "No actions saved"
        override val isComplete: Boolean = false
    }

    data object AllComplete : HistoryDetailCompletionSummary {
        override val label: String = "All completed!"
        override val isComplete: Boolean = true
    }

    data class Partial(
        val completedCount: Int,
        val totalCount: Int,
    ) : HistoryDetailCompletionSummary {
        override val label: String = "$completedCount of $totalCount completed"
        override val isComplete: Boolean = false
    }
}
