package com.robertgasparian.routinehelper.ui.daily

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.features.routinetracking.BuildConfig
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent

@Composable
fun DailyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DailyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RoutineTrackingComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                RoutineTrackingIntent.CreateActionClick -> onCreateActionClick()
                is RoutineTrackingIntent.EditActionClick -> onEditActionClick(intent.actionId)
                else -> viewModel.onIntent(intent)
            }
        },
        showSnapshotAction = BuildConfig.DEBUG,
        onSettingsClick = onSettingsClick,
    )
}
