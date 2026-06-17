package com.robertgasparian.routinehelper.ui.daily

sealed interface DailyUiEvent {
    sealed interface State : DailyUiEvent

    data object CreateActionClick : DailyUiEvent

    data class EditActionClick(
        val actionId: Long,
    ) : DailyUiEvent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : State

    data class CompletedCountChange(
        val routineItemId: Long,
        val completedCount: Int,
    ) : State

    data class HiddenChange(
        val routineItemId: Long,
        val isHidden: Boolean,
    ) : State

    data class ReorderItems(
        val routineItemIdsInOrder: List<Long>,
    ) : State

    data object SnapshotClick : State

    data class SnapshotDateSelected(
        val date: String,
    ) : State

    data class EditNoteClick(
        val item: DailyItemUiState,
    ) : State

    data object EditSummaryNoteClick : State

    data class NoteDraftChange(
        val value: NoteDraftUiState,
    ) : State

    data object NoteDraftClearClick : State

    data object NoteDraftDateClick : State

    data object NoteDraftWeekdayClick : State

    data object NoteDraftTimeClick : State

    data object NoteEditorDismiss : State

    data object NoteEditorSaveClick : State
}
