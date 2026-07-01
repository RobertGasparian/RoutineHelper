package com.robertgasparian.routinehelper.ui.tracking

sealed interface RoutineTrackingIntent {
    data object CreateActionClick : RoutineTrackingIntent

    data class EditActionClick(
        val actionId: Long,
    ) : RoutineTrackingIntent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : RoutineTrackingIntent

    data class CompletedCountChange(
        val routineItemId: Long,
        val completedCount: Int,
    ) : RoutineTrackingIntent

    data class HiddenChange(
        val routineItemId: Long,
        val isHidden: Boolean,
    ) : RoutineTrackingIntent

    data class ReorderItems(
        val routineItemIdsInOrder: List<Long>,
    ) : RoutineTrackingIntent

    data object SnapshotClick : RoutineTrackingIntent

    data class SnapshotDateSelected(
        val date: String,
    ) : RoutineTrackingIntent

    data class EditNoteClick(
        val item: RoutineTrackingItemUiState,
    ) : RoutineTrackingIntent

    data object EditSummaryNoteClick : RoutineTrackingIntent

    data class NoteDraftChange(
        val value: NoteDraftUiState,
    ) : RoutineTrackingIntent

    data object NoteDraftClearClick : RoutineTrackingIntent

    data object NoteDraftDateClick : RoutineTrackingIntent

    data object NoteDraftWeekdayClick : RoutineTrackingIntent

    data object NoteDraftTimeClick : RoutineTrackingIntent

    data object NoteEditorDismiss : RoutineTrackingIntent

    data object NoteEditorSaveClick : RoutineTrackingIntent
}

