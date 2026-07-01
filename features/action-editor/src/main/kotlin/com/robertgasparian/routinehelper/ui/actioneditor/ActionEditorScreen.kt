package com.robertgasparian.routinehelper.ui.actioneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

sealed interface ActionEditorIntent {
    data object BackClick : ActionEditorIntent

    data class TitleChange(
        val title: String,
    ) : ActionEditorIntent

    data class DescriptionChange(
        val description: String,
    ) : ActionEditorIntent

    data class RepeatEnabledChange(
        val enabled: Boolean,
    ) : ActionEditorIntent

    data class RepeatTargetCountChange(
        val targetCount: Int,
    ) : ActionEditorIntent

    data object SaveClick : ActionEditorIntent

    data object DeleteClick : ActionEditorIntent
}

sealed interface ActionEditorUiEvent {
    data object Saved : ActionEditorUiEvent

    data object Deleted : ActionEditorUiEvent
}

@Composable
fun ActionEditorScreen(
    actionId: Long?,
    cadence: RoutineCadence,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActionEditorViewModel = hiltViewModel<ActionEditorViewModel, ActionEditorViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                actionId = actionId,
                cadence = cadence,
            )
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                ActionEditorUiEvent.Deleted,
                ActionEditorUiEvent.Saved -> onBackClick()
            }
        }
    }

    ActionEditorComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                ActionEditorIntent.BackClick -> onBackClick()
                else -> viewModel.onIntent(intent)
            }
        },
        modifier = modifier,
        cadence = cadence,
    )
}
