package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.ui.text.input.TextFieldValue

sealed interface RoutineNoteDialogIntent {
    data class ValueChange(
        val value: TextFieldValue,
    ) : RoutineNoteDialogIntent

    data object Dismiss : RoutineNoteDialogIntent

    data object SaveClick : RoutineNoteDialogIntent

    data object ClearClick : RoutineNoteDialogIntent

    data object DateClick : RoutineNoteDialogIntent

    data object WeekdayClick : RoutineNoteDialogIntent

    data object TimeClick : RoutineNoteDialogIntent
}
