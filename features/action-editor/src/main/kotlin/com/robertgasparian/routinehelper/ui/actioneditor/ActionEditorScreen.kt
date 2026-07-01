package com.robertgasparian.routinehelper.ui.actioneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

sealed interface ActionEditorUiEvent {
    data object BackClick : ActionEditorUiEvent

    data class TitleChange(
        val title: String,
    ) : ActionEditorUiEvent

    data class DescriptionChange(
        val description: String,
    ) : ActionEditorUiEvent

    data class RepeatEnabledChange(
        val enabled: Boolean,
    ) : ActionEditorUiEvent

    data class RepeatTargetCountChange(
        val targetCount: Int,
    ) : ActionEditorUiEvent

    data object SaveClick : ActionEditorUiEvent

    data object DeleteClick : ActionEditorUiEvent
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

    ActionEditorComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                ActionEditorUiEvent.BackClick -> onBackClick()
                is ActionEditorUiEvent.DescriptionChange -> viewModel.updateDescription(event.description)
                is ActionEditorUiEvent.RepeatEnabledChange -> viewModel.updateRepeatEnabled(event.enabled)
                is ActionEditorUiEvent.RepeatTargetCountChange -> viewModel.updateRepeatTargetCount(event.targetCount)
                ActionEditorUiEvent.SaveClick -> viewModel.save(onSaved = onBackClick)
                ActionEditorUiEvent.DeleteClick -> viewModel.delete(
                    onDeleted = onBackClick,
                )
                is ActionEditorUiEvent.TitleChange -> viewModel.updateTitle(event.title)
            }
        },
        modifier = modifier,
        cadence = cadence,
    )
}
