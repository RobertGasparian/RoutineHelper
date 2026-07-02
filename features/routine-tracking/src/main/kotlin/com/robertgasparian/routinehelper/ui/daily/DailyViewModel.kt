package com.robertgasparian.routinehelper.ui.daily

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
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
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import com.robertgasparian.routinehelper.ui.tracking.insertAtCursor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

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
) : BaseViewModel<RoutineTrackingUiState, RoutineTrackingIntent, Nothing>() {
    private val todayDate = timeProvider.currentDate().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    override val uiState: StateFlow<RoutineTrackingUiState> =
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
            .stateInViewModel(initialValue = RoutineTrackingUiState(date = todayDate))

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
            RoutineTrackingIntent.SnapshotClick -> snapshotDaily()
            is RoutineTrackingIntent.SnapshotDateSelected -> snapshotDaily(snapshotDate = intent.date)
            is RoutineTrackingIntent.EditNoteClick -> showItemNoteEditor(
                routineItemId = intent.routineItemId,
                note = intent.note,
                itemTitle = intent.itemTitle,
            )
            RoutineTrackingIntent.EditSummaryNoteClick -> showSummaryNoteEditor(uiState.value.summaryNote)
            is RoutineTrackingIntent.NoteDraftChange -> updateNoteDraft(intent)
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
        launch {
            setTodayItemHiddenUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        launch {
            reorderDailyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        launch {
            updateTodayItemNoteUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    private fun updateSummaryNote(note: String) {
        launch {
            updateTodaySummaryNoteUseCase(
                date = todayDate,
                note = note,
            )
        }
    }

    private fun showItemNoteEditor(
        routineItemId: Long,
        note: String,
        itemTitle: String,
    ) {
        noteEditor.value = NoteEditorUiState.item(
            routineItemId = routineItemId,
            note = note,
            isWeekly = false,
            itemTitle = itemTitle,
        )
    }

    private fun showSummaryNoteEditor(summaryNote: String) {
        noteEditor.value = NoteEditorUiState.summary(
            note = summaryNote,
            isWeekly = false,
        )
    }

    private fun updateNoteDraft(intent: RoutineTrackingIntent.NoteDraftChange) {
        noteEditor.value = noteEditor.value?.copy(
            value = NoteDraftUiState(
                text = intent.text,
                selectionStart = intent.selectionStart,
                selectionEnd = intent.selectionEnd,
            ),
        )
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
        launch {
            finalizeTodayUseCase(
                date = todayDate,
                snapshotPeriodStartDate = snapshotDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}
