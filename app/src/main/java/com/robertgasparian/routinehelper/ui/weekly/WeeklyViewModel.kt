package com.robertgasparian.routinehelper.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.core.time.TimeProvider
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
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiEvent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import com.robertgasparian.routinehelper.ui.tracking.insertAtCursor
import com.robertgasparian.routinehelper.work.SnapshotWorkDates
import com.robertgasparian.routinehelper.work.startOfCalendarWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
) : ViewModel() {
    private val weekStartDate = timeProvider.currentDate().startOfWeek().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    val uiState: StateFlow<RoutineTrackingUiState> =
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RoutineTrackingUiState(date = "Week of $weekStartDate"),
            )

    fun onEvent(event: RoutineTrackingUiEvent.Intent) {
        when (event) {
            is RoutineTrackingUiEvent.CheckedChange -> setChecked(
                routineItemId = event.routineItemId,
                isChecked = event.isChecked,
            )
            is RoutineTrackingUiEvent.CompletedCountChange -> updateCompletedCount(
                routineItemId = event.routineItemId,
                completedCount = event.completedCount,
            )
            is RoutineTrackingUiEvent.HiddenChange -> setHidden(
                routineItemId = event.routineItemId,
                isHidden = event.isHidden,
            )
            is RoutineTrackingUiEvent.ReorderItems -> reorderItems(event.routineItemIdsInOrder)
            RoutineTrackingUiEvent.SnapshotClick -> snapshotWeek()
            is RoutineTrackingUiEvent.SnapshotDateSelected -> snapshotWeek(snapshotWeekStartDate = event.date)
            is RoutineTrackingUiEvent.EditNoteClick -> showItemNoteEditor(event.item)
            RoutineTrackingUiEvent.EditSummaryNoteClick -> showSummaryNoteEditor(uiState.value.summaryNote)
            is RoutineTrackingUiEvent.NoteDraftChange -> updateNoteDraft(event.value)
            RoutineTrackingUiEvent.NoteDraftClearClick -> clearNoteDraft()
            RoutineTrackingUiEvent.NoteDraftDateClick -> insertCurrentDateIntoNoteDraft()
            RoutineTrackingUiEvent.NoteDraftWeekdayClick -> insertCurrentWeekdayIntoNoteDraft()
            RoutineTrackingUiEvent.NoteDraftTimeClick -> insertCurrentTimeIntoNoteDraft()
            RoutineTrackingUiEvent.NoteEditorDismiss -> dismissNoteEditor()
            RoutineTrackingUiEvent.NoteEditorSaveClick -> saveNoteDraft()
        }
    }

    private fun setChecked(
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            setWeeklyItemHiddenUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        viewModelScope.launch {
            reorderWeeklyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        viewModelScope.launch {
            updateWeeklyItemNoteUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    private fun updateSummaryNote(note: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            updateWeeklyItemCompletedCountUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    private fun snapshotWeek(
        // TODO Remove this test-only override when debug snapshot controls are removed.
        snapshotWeekStartDate: String = SnapshotWorkDates
            .previousCompletedCalendarWeekStartDate(timeProvider.now())
            .toString(),
    ) {
        viewModelScope.launch {
            finalizeWeeklyUseCase(
                weekStartDate = weekStartDate,
                snapshotWeekStartDate = snapshotWeekStartDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate {
    return startOfCalendarWeek()
}
