package com.robertgasparian.routinehelper.ui.weekly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.routinetracking.BuildConfig
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSession

@Composable
fun WeeklyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    onSummaryEditorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    reflectionEditorSession: ReflectionEditorSession,
    viewModel: WeeklyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reflectionState by reflectionEditorSession.state.collectAsStateWithLifecycle()

    LaunchedEffect(reflectionState.saveRequest?.requestId) {
        val request = reflectionState.saveRequest ?: return@LaunchedEffect
        viewModel.onIntent(RoutineTrackingIntent.SaveSummaryNote(request.text))
        reflectionEditorSession.consumeSaveRequest(request.requestId)
    }

    RoutineTrackingComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                RoutineTrackingIntent.CreateActionClick -> onCreateActionClick()
                RoutineTrackingIntent.SettingsClick -> onSettingsClick()
                is RoutineTrackingIntent.EditActionClick -> onEditActionClick(intent.actionId)
                RoutineTrackingIntent.EditSummaryNoteClick -> {
                    reflectionEditorSession.start(uiState.summaryNote)
                    onSummaryEditorClick()
                }
                else -> viewModel.onIntent(intent)
            }
        },
        cadence = RoutineCadence.Weekly,
        showSnapshotAction = BuildConfig.DEBUG,
        showAddTestItems = BuildConfig.DEBUG,
    )
}
