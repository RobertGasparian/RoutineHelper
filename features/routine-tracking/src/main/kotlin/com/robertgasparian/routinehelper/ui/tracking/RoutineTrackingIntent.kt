package com.robertgasparian.routinehelper.ui.tracking

sealed interface RoutineTrackingIntent {
    data object CreateActionClick : RoutineTrackingIntent

    data object SettingsClick : RoutineTrackingIntent

    data object AddTestItemsClick : RoutineTrackingIntent

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

    data class RemoveItem(
        val routineItemId: Long,
    ) : RoutineTrackingIntent

    data class ReorderItems(
        val routineItemIdsInOrder: List<Long>,
    ) : RoutineTrackingIntent

    data object SnapshotClick : RoutineTrackingIntent

    data class SnapshotDateSelected(
        val date: String,
    ) : RoutineTrackingIntent

    data class EditNoteClick(
        val routineItemId: Long,
        val itemTitle: String,
        val note: String,
    ) : RoutineTrackingIntent

    data object EditSummaryNoteClick : RoutineTrackingIntent

    data class SaveSummaryNote(
        val note: String,
    ) : RoutineTrackingIntent

    data class NoteDraftChange(
        val text: String,
        val selectionStart: Int,
        val selectionEnd: Int = selectionStart,
    ) : RoutineTrackingIntent

    data object NoteDraftClearClick : RoutineTrackingIntent

    data object NoteDraftDateClick : RoutineTrackingIntent

    data object NoteDraftWeekdayClick : RoutineTrackingIntent

    data object NoteDraftTimeClick : RoutineTrackingIntent

    data object NoteEditorDismiss : RoutineTrackingIntent

    data object NoteEditorSaveClick : RoutineTrackingIntent
}

