package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.ui.share.ShareDraft

sealed interface HistoryDetailIntent {
    data object BackClick : HistoryDetailIntent

    data object DebugSummaryNotificationClick : HistoryDetailIntent

    data object ShareClick : HistoryDetailIntent

    data object ShareAsTextClick : HistoryDetailIntent

    data object ShareAsFileClick : HistoryDetailIntent

    data class ShareTextChange(
        val text: String,
    ) : HistoryDetailIntent

    data class ShareFileNameChange(
        val fileName: String,
    ) : HistoryDetailIntent

    data object ShareDismiss : HistoryDetailIntent

    data object EditSummaryNoteClick : HistoryDetailIntent

    data class SummaryNoteDraftChange(
        val text: String,
        val selectionStart: Int,
        val selectionEnd: Int = selectionStart,
    ) : HistoryDetailIntent

    data object SummaryNoteDraftClearClick : HistoryDetailIntent

    data object SummaryNoteEditorDismiss : HistoryDetailIntent

    data object SummaryNoteEditorSaveClick : HistoryDetailIntent

    data class ShareTextConfirm(
        val messageText: String,
    ) : HistoryDetailIntent

    data class ShareFileConfirm(
        val draft: ShareDraft.File,
    ) : HistoryDetailIntent

    data object DeleteClick : HistoryDetailIntent
}
