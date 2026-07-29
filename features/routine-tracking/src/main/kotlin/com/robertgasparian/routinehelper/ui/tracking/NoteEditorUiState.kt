package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDraftUiState

data class NoteEditorUiState(
    val target: NoteEditorTarget,
    val cadence: RoutineCadence,
    val itemTitle: String? = null,
    val value: RoutineNoteDraftUiState,
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
                target = NoteEditorTarget(routineItemId),
                cadence = cadence,
                itemTitle = itemTitle,
                value = RoutineNoteDraftUiState.fromText(note),
            )
        }
    }
}

data class NoteEditorTarget(
    val routineItemId: Long,
)
