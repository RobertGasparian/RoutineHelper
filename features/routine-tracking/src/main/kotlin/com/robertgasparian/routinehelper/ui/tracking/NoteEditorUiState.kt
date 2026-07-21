package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

data class NoteEditorUiState(
    val target: NoteEditorTarget,
    val cadence: RoutineCadence,
    val itemTitle: String? = null,
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
            return NoteEditorUiState(
                target = NoteEditorTarget.Item(routineItemId),
                cadence = cadence,
                itemTitle = itemTitle,
                value = NoteDraftUiState.fromText(note),
            )
        }

        fun summary(
            note: String,
            cadence: RoutineCadence,
        ): NoteEditorUiState {
            return NoteEditorUiState(
                target = NoteEditorTarget.Summary,
                cadence = cadence,
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
