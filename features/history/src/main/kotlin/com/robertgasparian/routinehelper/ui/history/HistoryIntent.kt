package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.ui.share.ShareDraft

sealed interface HistoryIntent {
    data class SnapshotClick(
        val snapshotId: Long,
    ) : HistoryIntent

    data class ToggleSnapshotSelection(
        val snapshotId: Long,
    ) : HistoryIntent

    data object ClearSelectionClick : HistoryIntent

    data object ShareSelectedClick : HistoryIntent

    data object ShareAsTextClick : HistoryIntent

    data object ShareAsFileClick : HistoryIntent

    data object DeleteSelectedClick : HistoryIntent

    data class FilterClick(
        val filter: HistoryFilter,
    ) : HistoryIntent

    data class ShareTextChange(
        val text: String,
    ) : HistoryIntent

    data class ShareFileNameChange(
        val fileName: String,
    ) : HistoryIntent

    data object ShareDismiss : HistoryIntent

    data class ShareTextConfirm(
        val messageText: String,
    ) : HistoryIntent

    data class ShareFileConfirm(
        val draft: ShareDraft.File,
    ) : HistoryIntent
}
