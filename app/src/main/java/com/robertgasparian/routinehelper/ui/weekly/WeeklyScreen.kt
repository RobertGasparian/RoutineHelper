package com.robertgasparian.routinehelper.ui.weekly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.ui.today.TodayComponent
import com.robertgasparian.routinehelper.ui.today.TodayUiEvent

@Composable
fun WeeklyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    viewModel: WeeklyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is TodayUiEvent.CheckedChange -> viewModel.setChecked(
                    routineItemId = event.routineItemId,
                    isChecked = event.isChecked,
                )
                TodayUiEvent.CreateActionClick -> onCreateActionClick()
                is TodayUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                is TodayUiEvent.NoteChange -> viewModel.updateNote(
                    routineItemId = event.routineItemId,
                    note = event.note,
                )
                is TodayUiEvent.SnapshotClick -> Unit
            }
        },
        title = "Weekly",
        emptyTitle = "No weekly items yet",
        emptyDescription = "Add your first weekly action to start tracking this week.",
        showSnapshotAction = false,
    )
}
