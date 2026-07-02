package com.robertgasparian.routinehelper.ui.share

sealed interface ShareTextPreviewIntent {
    data object BackClick : ShareTextPreviewIntent

    data object CancelClick : ShareTextPreviewIntent

    data object ShareClick : ShareTextPreviewIntent

    data class TextChange(
        val text: String,
    ) : ShareTextPreviewIntent
}
