package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

data class RoutineTrackingUiState(
    val date: String,
    val summaryNote: String = "",
    val items: List<RoutineTrackingItemUiState> = emptyList(),
    val noteEditor: NoteEditorUiState? = null,
) {
    companion object {
        fun preview(): RoutineTrackingUiState =
            RoutineTrackingUiState(
                date = "2026-05-29",
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineTrackingItemUiState(
                        routineItemId = 1,
                        actionId = 101,
                        title = "Drink water",
                        description = "Drink 3L water",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 2,
                        actionId = 102,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        note = "",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 3,
                        actionId = 103,
                        title = "Read",
                        description = null,
                        repeatTargetCount = 5,
                        completedCount = 2,
                        isChecked = false,
                        note = "Finish the last chapter tonight.",
                    ),
                    RoutineTrackingItemUiState(
                        routineItemId = 4,
                        actionId = 104,
                        title = "Run",
                        description = "Rest day for the knee",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        isHidden = true,
                        note = "Skipped intentionally today.",
                    ),
                ),
            )

        fun previewEmpty(): RoutineTrackingUiState =
            RoutineTrackingUiState(date = "2026-05-29")
    }
}

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

data class NoteDraftUiState(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int = selectionStart,
) {
    companion object {
        fun fromText(text: String): NoteDraftUiState =
            NoteDraftUiState(
                text = text,
                selectionStart = text.length,
                selectionEnd = text.length,
            )
    }
}

fun NoteDraftUiState.insertAtCursor(textToInsert: String): NoteDraftUiState {
    val start = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val end = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val updatedText = text.replaceRange(start, end, textToInsert)
    val updatedCursor = start + textToInsert.length
    return NoteDraftUiState(
        text = updatedText,
        selectionStart = updatedCursor,
        selectionEnd = updatedCursor,
    )
}

data class RoutineTrackingItemUiState(
    val routineItemId: Long,
    val actionId: Long,
    val title: String,
    val description: String?,
    val repeatTargetCount: Int?,
    val completedCount: Int,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val note: String,
) {
    val isRepeatAction: Boolean = repeatTargetCount != null
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
