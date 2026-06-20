package com.robertgasparian.routinehelper.ui.tracking

sealed interface RoutineTrackingUiEvent {
    sealed interface Intent : RoutineTrackingUiEvent

    data object CreateActionClick : RoutineTrackingUiEvent

    data class EditActionClick(
        val actionId: Long,
    ) : RoutineTrackingUiEvent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : Intent

    data class CompletedCountChange(
        val routineItemId: Long,
        val completedCount: Int,
    ) : Intent

    data class HiddenChange(
        val routineItemId: Long,
        val isHidden: Boolean,
    ) : Intent

    data class ReorderItems(
        val routineItemIdsInOrder: List<Long>,
    ) : Intent

    data object SnapshotClick : Intent

    data class SnapshotDateSelected(
        val date: String,
    ) : Intent

    data class EditNoteClick(
        val item: RoutineTrackingItemUiState,
    ) : Intent

    data object EditSummaryNoteClick : Intent

    data class NoteDraftChange(
        val value: NoteDraftUiState,
    ) : Intent

    data object NoteDraftClearClick : Intent

    data object NoteDraftDateClick : Intent

    data object NoteDraftWeekdayClick : Intent

    data object NoteDraftTimeClick : Intent

    data object NoteEditorDismiss : Intent

    data object NoteEditorSaveClick : Intent
}
