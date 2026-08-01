package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.ReflectionRating
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

    data object EditReflectionClick : HistoryDetailIntent

    data class SaveReflection(
        val summaryNote: String,
        val rating: ReflectionRating?,
    ) : HistoryDetailIntent

    data class ShareTextConfirm(
        val messageText: String,
    ) : HistoryDetailIntent

    data class ShareFileConfirm(
        val draft: ShareDraft.File,
    ) : HistoryDetailIntent

    data object DeleteClick : HistoryDetailIntent
}
