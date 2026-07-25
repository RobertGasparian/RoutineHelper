package com.robertgasparian.routinehelper.ui.tracking

import androidx.compose.ui.text.input.TextFieldValue
import com.robertgasparian.routinehelper.ui.dsm.toRoutineNoteDraftUiState

fun TextFieldValue.toNoteDraftChange(): RoutineTrackingIntent.NoteDraftChange {
    val draft = toRoutineNoteDraftUiState()
    return RoutineTrackingIntent.NoteDraftChange(
        text = draft.text,
        selectionStart = draft.selectionStart,
        selectionEnd = draft.selectionEnd,
    )
}
