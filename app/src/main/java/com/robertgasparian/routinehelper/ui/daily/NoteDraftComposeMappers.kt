package com.robertgasparian.routinehelper.ui.daily

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun NoteDraftUiState.toTextFieldValue(): TextFieldValue =
    TextFieldValue(
        text = text,
        selection = TextRange(
            start = selectionStart.coerceIn(0, text.length),
            end = selectionEnd.coerceIn(0, text.length),
        ),
    )

fun TextFieldValue.toNoteDraftUiState(): NoteDraftUiState =
    NoteDraftUiState(
        text = text,
        selectionStart = selection.start.coerceIn(0, text.length),
        selectionEnd = selection.end.coerceIn(0, text.length),
    )
