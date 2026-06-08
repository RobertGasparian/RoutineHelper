package com.robertgasparian.routinehelper.ui.weekly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.BuildConfig
import com.robertgasparian.routinehelper.ui.daily.DailyComponent
import com.robertgasparian.routinehelper.ui.daily.DailyUiEvent

@Composable
fun WeeklyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    viewModel: WeeklyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is DailyUiEvent.CheckedChange -> viewModel.setChecked(
                    routineItemId = event.routineItemId,
                    isChecked = event.isChecked,
                )
                is DailyUiEvent.CompletedCountChange -> viewModel.updateCompletedCount(
                    routineItemId = event.routineItemId,
                    completedCount = event.completedCount,
                )
                DailyUiEvent.CreateActionClick -> onCreateActionClick()
                is DailyUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                DailyUiEvent.SnapshotClick -> viewModel.snapshotWeek()
                is DailyUiEvent.EditNoteClick -> viewModel.showItemNoteEditor(event.item)
                DailyUiEvent.EditSummaryNoteClick -> viewModel.showSummaryNoteEditor(uiState.summaryNote)
                is DailyUiEvent.NoteDraftChange -> viewModel.updateNoteDraft(event.value)
                DailyUiEvent.NoteDraftClearClick -> viewModel.clearNoteDraft()
                DailyUiEvent.NoteDraftDateClick -> viewModel.insertCurrentDateIntoNoteDraft()
                DailyUiEvent.NoteDraftWeekdayClick -> viewModel.insertCurrentWeekdayIntoNoteDraft()
                DailyUiEvent.NoteDraftTimeClick -> viewModel.insertCurrentTimeIntoNoteDraft()
                DailyUiEvent.NoteEditorDismiss -> viewModel.dismissNoteEditor()
                DailyUiEvent.NoteEditorSaveClick -> viewModel.saveNoteDraft()
            }
        },
        title = "Weekly",
        emptyTitle = "No weekly items yet",
        emptyDescription = "Add your first weekly action to start tracking this week.",
        showSnapshotAction = BuildConfig.DEBUG,
    )
}
