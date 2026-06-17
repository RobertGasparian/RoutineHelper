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
    onSettingsClick: () -> Unit,
    viewModel: WeeklyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                DailyUiEvent.CreateActionClick -> onCreateActionClick()
                is DailyUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                is DailyUiEvent.Intent -> viewModel.onEvent(event)
            }
        },
        title = "Weekly",
        emptyTitle = "No weekly items yet",
        emptyDescription = "Add your first weekly action to start tracking this week.",
        showSnapshotAction = BuildConfig.DEBUG,
        onSettingsClick = onSettingsClick,
    )
}
