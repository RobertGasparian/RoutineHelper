package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

data class NoteEditorUiState(
    val target: NoteEditorTarget,
    val title: String,
    val supportingText: String,
    val label: String,
    val value: NoteDraftUiState,
) {
    val canClear: Boolean = value.text.isNotBlank()

    companion object {
        fun item(
            routineItemId: Long,
            note: String,
            cadence: RoutineCadence,
            itemTitle: String,
        ): NoteEditorUiState {
            val label = cadence.itemNoteLabel
            return NoteEditorUiState(
                target = NoteEditorTarget.Item(routineItemId),
                title = if (note.isBlank()) "Add note" else "Edit note",
                supportingText = "$label for $itemTitle",
                label = label,
                value = NoteDraftUiState.fromText(note),
            )
        }

        fun summary(
            note: String,
            cadence: RoutineCadence,
        ): NoteEditorUiState {
            val label = cadence.summaryNoteLabel
            return NoteEditorUiState(
                target = NoteEditorTarget.Summary,
                title = label,
                supportingText = cadence.summaryNoteSupportingText,
                label = label,
                value = NoteDraftUiState.fromText(note),
            )
        }
    }
}

sealed interface NoteEditorTarget {
    data class Item(
        val routineItemId: Long,
    ) : NoteEditorTarget

    data object Summary : NoteEditorTarget
}

private val RoutineCadence.itemNoteLabel: String
    get() = when (this) {
        RoutineCadence.Daily -> "Daily note"
        RoutineCadence.Weekly -> "Weekly note"
    }

private val RoutineCadence.summaryNoteLabel: String
    get() = when (this) {
        RoutineCadence.Daily -> "Day note"
        RoutineCadence.Weekly -> "Week note"
    }

private val RoutineCadence.summaryNoteSupportingText: String
    get() = when (this) {
        RoutineCadence.Daily -> "This note is saved for this day only."
        RoutineCadence.Weekly -> "This note is saved for the current week."
    }
