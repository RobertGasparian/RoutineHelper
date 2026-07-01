package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.core.time.startOfCalendarWeek
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderWeeklyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.ui.tracking.NoteDateTimeTextProvider
import com.robertgasparian.routinehelper.ui.tracking.NoteDraftUiState
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorTarget
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import com.robertgasparian.routinehelper.ui.tracking.insertAtCursor
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class WeeklyViewModel @Inject constructor(
    weeklyItemsUseCase: WeeklyItemsUseCase,
    weeklySummaryNoteUseCase: WeeklySummaryNoteUseCase,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val reorderWeeklyRoutineItemsUseCase: ReorderWeeklyRoutineItemsUseCase,
    private val setWeeklyItemCheckedUseCase: SetWeeklyItemCheckedUseCase,
    private val setWeeklyItemHiddenUseCase: SetWeeklyItemHiddenUseCase,
    private val updateWeeklyItemCompletedCountUseCase: UpdateWeeklyItemCompletedCountUseCase,
    private val updateWeeklyItemNoteUseCase: UpdateWeeklyItemNoteUseCase,
    private val updateWeeklySummaryNoteUseCase: UpdateWeeklySummaryNoteUseCase,
    private val noteDateTimeTextProvider: NoteDateTimeTextProvider,
    private val timeProvider: TimeProvider,
) : BaseViewModel<RoutineTrackingUiState, RoutineTrackingIntent, Nothing>() {
    private val weekStartDate = timeProvider.currentDate().startOfWeek().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    override val uiState: StateFlow<RoutineTrackingUiState> =
        combine(
            weeklyItemsUseCase(weekStartDate),
            weeklySummaryNoteUseCase(weekStartDate),
            noteEditor,
        ) { items, summaryNote, noteEditor ->
            RoutineTrackingUiState(
                date = "Week of $weekStartDate",
                summaryNote = summaryNote.orEmpty(),
                items = items.map { item -> item.toRoutineTrackingItemUiState() },
                noteEditor = noteEditor,
            )
        }
            .stateInViewModel(initialValue = RoutineTrackingUiState(date = "Week of $weekStartDate"))

    override fun handleIntent(intent: RoutineTrackingIntent) {
        when (intent) {
            RoutineTrackingIntent.CreateActionClick,
            is RoutineTrackingIntent.EditActionClick -> Unit
            is RoutineTrackingIntent.CheckedChange -> setChecked(
                routineItemId = intent.routineItemId,
                isChecked = intent.isChecked,
            )
            is RoutineTrackingIntent.CompletedCountChange -> updateCompletedCount(
                routineItemId = intent.routineItemId,
                completedCount = intent.completedCount,
            )
            is RoutineTrackingIntent.HiddenChange -> setHidden(
                routineItemId = intent.routineItemId,
                isHidden = intent.isHidden,
            )
            is RoutineTrackingIntent.ReorderItems -> reorderItems(intent.routineItemIdsInOrder)
            RoutineTrackingIntent.SnapshotClick -> snapshotWeek()
            is RoutineTrackingIntent.SnapshotDateSelected -> snapshotWeek(snapshotWeekStartDate = intent.date)
            is RoutineTrackingIntent.EditNoteClick -> showItemNoteEditor(intent.item)
            RoutineTrackingIntent.EditSummaryNoteClick -> showSummaryNoteEditor(uiState.value.summaryNote)
            is RoutineTrackingIntent.NoteDraftChange -> updateNoteDraft(intent.value)
            RoutineTrackingIntent.NoteDraftClearClick -> clearNoteDraft()
            RoutineTrackingIntent.NoteDraftDateClick -> insertCurrentDateIntoNoteDraft()
            RoutineTrackingIntent.NoteDraftWeekdayClick -> insertCurrentWeekdayIntoNoteDraft()
            RoutineTrackingIntent.NoteDraftTimeClick -> insertCurrentTimeIntoNoteDraft()
            RoutineTrackingIntent.NoteEditorDismiss -> dismissNoteEditor()
            RoutineTrackingIntent.NoteEditorSaveClick -> saveNoteDraft()
        }
    }

    private fun setChecked(
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        launch {
            setWeeklyItemCheckedUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isChecked = isChecked,
            )
        }
    }

    private fun setHidden(
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        launch {
            setWeeklyItemHiddenUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        launch {
            reorderWeeklyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        launch {
            updateWeeklyItemNoteUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    private fun updateSummaryNote(note: String) {
        launch {
            updateWeeklySummaryNoteUseCase(
                weekStartDate = weekStartDate,
                note = note,
            )
        }
    }

    private fun showItemNoteEditor(item: RoutineTrackingItemUiState) {
        noteEditor.value = NoteEditorUiState.item(
            routineItemId = item.routineItemId,
            note = item.note,
            isWeekly = true,
            itemTitle = item.title,
        )
    }

    private fun showSummaryNoteEditor(summaryNote: String) {
        noteEditor.value = NoteEditorUiState.summary(
            note = summaryNote,
            isWeekly = true,
        )
    }

    private fun updateNoteDraft(value: NoteDraftUiState) {
        noteEditor.value = noteEditor.value?.copy(value = value)
    }

    private fun insertCurrentDateIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentDateText())
    }

    private fun insertCurrentWeekdayIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentWeekdayText())
    }

    private fun insertCurrentTimeIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentTimeText())
    }

    private fun clearNoteDraft() {
        noteEditor.value = noteEditor.value?.copy(value = NoteDraftUiState.fromText(""))
    }

    private fun dismissNoteEditor() {
        noteEditor.value = null
    }

    private fun saveNoteDraft() {
        val editor = noteEditor.value ?: return
        when (val target = editor.target) {
            is NoteEditorTarget.Item -> updateNote(
                routineItemId = target.routineItemId,
                note = editor.value.text,
            )
            NoteEditorTarget.Summary -> updateSummaryNote(editor.value.text)
        }
        noteEditor.value = null
    }

    private fun insertTextIntoNoteDraft(text: String) {
        noteEditor.value = noteEditor.value?.let { editor ->
            editor.copy(value = editor.value.insertAtCursor(text))
        }
    }

    private fun updateCompletedCount(
        routineItemId: Long,
        completedCount: Int,
    ) {
        launch {
            updateWeeklyItemCompletedCountUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    private fun snapshotWeek(
        // TODO Remove this test-only override when debug snapshot controls are removed.
        snapshotWeekStartDate: String = SnapshotDates
            .previousCompletedCalendarWeekStartDate(timeProvider.now())
            .toString(),
    ) {
        launch {
            finalizeWeeklyUseCase(
                weekStartDate = weekStartDate,
                snapshotPeriodStartDate = snapshotWeekStartDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate {
    return startOfCalendarWeek()
}
