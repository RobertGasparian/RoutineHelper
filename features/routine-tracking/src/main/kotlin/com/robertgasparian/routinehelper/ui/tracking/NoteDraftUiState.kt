package com.robertgasparian.routinehelper.ui.tracking

data class NoteDraftUiState(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int = selectionStart,
) {
    companion object {
        fun fromText(text: String): NoteDraftUiState =
            NoteDraftUiState(
                text = text,
                selectionStart = text.length,
                selectionEnd = text.length,
            )
    }
}

fun NoteDraftUiState.insertAtCursor(textToInsert: String): NoteDraftUiState {
    val start = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val end = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val updatedText = text.replaceRange(start, end, textToInsert)
    val updatedCursor = start + textToInsert.length
    return NoteDraftUiState(
        text = updatedText,
        selectionStart = updatedCursor,
        selectionEnd = updatedCursor,
    )
}
