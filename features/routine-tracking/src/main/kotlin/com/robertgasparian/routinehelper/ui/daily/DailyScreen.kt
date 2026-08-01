package com.robertgasparian.routinehelper.ui.daily

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.features.routinetracking.BuildConfig
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSession
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent

@Composable
fun DailyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    onSummaryEditorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    reflectionEditorSession: ReflectionEditorSession,
    viewModel: DailyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reflectionState by reflectionEditorSession.state.collectAsStateWithLifecycle()

    LaunchedEffect(reflectionState.saveRequest?.requestId) {
        val request = reflectionState.saveRequest ?: return@LaunchedEffect
        viewModel.onIntent(
            RoutineTrackingIntent.SaveReflection(
                summaryNote = request.text,
                rating = request.rating,
            ),
        )
        reflectionEditorSession.consumeSaveRequest(request.requestId)
    }

    RoutineTrackingComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                RoutineTrackingIntent.CreateActionClick -> onCreateActionClick()
                RoutineTrackingIntent.SettingsClick -> onSettingsClick()
                is RoutineTrackingIntent.EditActionClick -> onEditActionClick(intent.actionId)
                RoutineTrackingIntent.EditReflectionClick -> {
                    reflectionEditorSession.start(
                        ReflectionEditorInitialState(
                            text = uiState.summaryNote,
                            rating = uiState.rating,
                        ),
                    )
                    onSummaryEditorClick()
                }
                else -> viewModel.onIntent(intent)
            }
        },
        showSnapshotAction = BuildConfig.DEBUG,
        showAddTestItems = BuildConfig.DEBUG,
    )
}
