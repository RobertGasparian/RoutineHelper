package com.robertgasparian.routinehelper.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.work.SnapshotWorkDates
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
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
    private val setTodayItemCheckedUseCase: SetTodayItemCheckedUseCase,
    private val updateTodayItemCompletedCountUseCase: UpdateTodayItemCompletedCountUseCase,
    private val updateTodayItemNoteUseCase: UpdateTodayItemNoteUseCase,
    private val updateTodaySummaryNoteUseCase: UpdateTodaySummaryNoteUseCase,
    private val noteDateTimeTextProvider: NoteDateTimeTextProvider,
) : ViewModel() {
    private val todayDate = LocalDate.now().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    val uiState: StateFlow<DailyUiState> =
        combine(
            todayItemsUseCase(todayDate),
            todaySummaryNoteUseCase(todayDate),
            noteEditor,
        ) { items, summaryNote, noteEditor ->
            DailyUiState(
                date = todayDate,
                summaryNote = summaryNote.orEmpty(),
                items = items.map(TodayRoutineItem::toUiState),
                noteEditor = noteEditor,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyUiState(date = todayDate),
            )

    fun setChecked(
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

    fun updateNote(
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

    fun updateSummaryNote(note: String) {
        viewModelScope.launch {
            updateTodaySummaryNoteUseCase(
                date = todayDate,
                note = note,
            )
        }
    }

    fun showItemNoteEditor(item: DailyItemUiState) {
        noteEditor.value = NoteEditorUiState.item(
            routineItemId = item.routineItemId,
            note = item.note,
            isWeekly = false,
            itemTitle = item.title,
        )
    }

    fun showSummaryNoteEditor(summaryNote: String) {
        noteEditor.value = NoteEditorUiState.summary(
            note = summaryNote,
            isWeekly = false,
        )
    }

    fun updateNoteDraft(value: NoteDraftUiState) {
        noteEditor.value = noteEditor.value?.copy(value = value)
    }

    fun insertCurrentDateIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentDateText())
    }

    fun insertCurrentWeekdayIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentWeekdayText())
    }

    fun insertCurrentTimeIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentTimeText())
    }

    fun clearNoteDraft() {
        noteEditor.value = noteEditor.value?.copy(value = NoteDraftUiState.fromText(""))
    }

    fun dismissNoteEditor() {
        noteEditor.value = null
    }

    fun saveNoteDraft() {
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

    fun updateCompletedCount(
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

    fun snapshotDaily() {
        viewModelScope.launch {
            val snapshotDate = SnapshotWorkDates.dailySnapshotDate(ZonedDateTime.now()).toString()
            finalizeTodayUseCase(
                date = todayDate,
                snapshotDate = snapshotDate,
                finalizedAtMillis = System.currentTimeMillis(),
            )
        }
    }
}

private fun TodayRoutineItem.toUiState(): DailyItemUiState =
    DailyItemUiState(
        routineItemId = routineItemId,
        actionId = actionId,
        title = title,
        description = description,
        repeatTargetCount = repeatTargetCount,
        completedCount = completedCount,
        isChecked = isChecked,
        note = note.orEmpty(),
    )
