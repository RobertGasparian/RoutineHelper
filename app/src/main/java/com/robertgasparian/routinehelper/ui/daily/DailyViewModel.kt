package com.robertgasparian.routinehelper.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderDailyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.ui.tracking.NoteDateTimeTextProvider
import com.robertgasparian.routinehelper.ui.tracking.NoteDraftUiState
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorTarget
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiEvent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import com.robertgasparian.routinehelper.ui.tracking.insertAtCursor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DailyViewModel @Inject constructor(
    todayItemsUseCase: TodayItemsUseCase,
    todaySummaryNoteUseCase: TodaySummaryNoteUseCase,
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val reorderDailyRoutineItemsUseCase: ReorderDailyRoutineItemsUseCase,
    private val setTodayItemCheckedUseCase: SetTodayItemCheckedUseCase,
    private val setTodayItemHiddenUseCase: SetTodayItemHiddenUseCase,
    private val updateTodayItemCompletedCountUseCase: UpdateTodayItemCompletedCountUseCase,
    private val updateTodayItemNoteUseCase: UpdateTodayItemNoteUseCase,
    private val updateTodaySummaryNoteUseCase: UpdateTodaySummaryNoteUseCase,
    private val noteDateTimeTextProvider: NoteDateTimeTextProvider,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val todayDate = timeProvider.currentDate().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    val uiState: StateFlow<RoutineTrackingUiState> =
        combine(
            todayItemsUseCase(todayDate),
            todaySummaryNoteUseCase(todayDate),
            noteEditor,
        ) { items, summaryNote, noteEditor ->
            RoutineTrackingUiState(
                date = todayDate,
                summaryNote = summaryNote.orEmpty(),
                items = items.map { item -> item.toRoutineTrackingItemUiState() },
                noteEditor = noteEditor,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RoutineTrackingUiState(date = todayDate),
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
            RoutineTrackingUiEvent.SnapshotClick -> snapshotDaily()
            is RoutineTrackingUiEvent.SnapshotDateSelected -> snapshotDaily(snapshotDate = event.date)
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
            setTodayItemCheckedUseCase(
                date = todayDate,
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
            setTodayItemHiddenUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        viewModelScope.launch {
            reorderDailyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        viewModelScope.launch {
            updateTodayItemNoteUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    private fun updateSummaryNote(note: String) {
        viewModelScope.launch {
            updateTodaySummaryNoteUseCase(
                date = todayDate,
                note = note,
            )
        }
    }

    private fun showItemNoteEditor(item: RoutineTrackingItemUiState) {
        noteEditor.value = NoteEditorUiState.item(
            routineItemId = item.routineItemId,
            note = item.note,
            isWeekly = false,
            itemTitle = item.title,
        )
    }

    private fun showSummaryNoteEditor(summaryNote: String) {
        noteEditor.value = NoteEditorUiState.summary(
            note = summaryNote,
            isWeekly = false,
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
            updateTodayItemCompletedCountUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    private fun snapshotDaily(
        // TODO Remove this test-only override when debug snapshot controls are removed.
        snapshotDate: String = SnapshotDates.dailySnapshotDate(timeProvider.now()).toString(),
    ) {
        viewModelScope.launch {
            finalizeTodayUseCase(
                date = todayDate,
                snapshotDate = snapshotDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}
