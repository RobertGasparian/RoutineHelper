package com.robertgasparian.routinehelper.ui.history.detail

sealed interface HistoryDetailCompletionSummary {
    val isComplete: Boolean

    data object Empty : HistoryDetailCompletionSummary {
        override val isComplete: Boolean = false
    }

    data object AllComplete : HistoryDetailCompletionSummary {
        override val isComplete: Boolean = true
    }

    data class Partial(
        val completedCount: Int,
        val totalCount: Int,
    ) : HistoryDetailCompletionSummary {
        override val isComplete: Boolean = false
    }
}
