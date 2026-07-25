package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class RoutineNoteDraftUiState(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int = selectionStart,
) {
    companion object {
        fun fromText(text: String): RoutineNoteDraftUiState =
            RoutineNoteDraftUiState(
                text = text,
                selectionStart = text.length,
                selectionEnd = text.length,
            )
    }
}

fun RoutineNoteDraftUiState.insertAtCursor(textToInsert: String): RoutineNoteDraftUiState {
    val start = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val end = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val updatedText = text.replaceRange(start, end, textToInsert)
    val updatedCursor = start + textToInsert.length
    return RoutineNoteDraftUiState(
        text = updatedText,
        selectionStart = updatedCursor,
        selectionEnd = updatedCursor,
    )
}

fun RoutineNoteDraftUiState.toTextFieldValue(): TextFieldValue =
    TextFieldValue(
        text = text,
        selection = TextRange(
            start = selectionStart.coerceIn(0, text.length),
            end = selectionEnd.coerceIn(0, text.length),
        ),
    )

fun TextFieldValue.toRoutineNoteDraftUiState(): RoutineNoteDraftUiState =
    RoutineNoteDraftUiState(
        text = text,
        selectionStart = selection.start.coerceIn(0, text.length),
        selectionEnd = selection.end.coerceIn(0, text.length),
    )
