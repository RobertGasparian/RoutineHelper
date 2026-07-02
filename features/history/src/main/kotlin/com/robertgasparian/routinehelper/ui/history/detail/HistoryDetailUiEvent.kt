package com.robertgasparian.routinehelper.ui.history.detail

sealed interface HistoryDetailUiEvent {
    data object SnapshotDeleted : HistoryDetailUiEvent
}
