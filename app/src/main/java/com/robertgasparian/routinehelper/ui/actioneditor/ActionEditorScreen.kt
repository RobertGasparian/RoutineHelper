package com.robertgasparian.routinehelper.ui.actioneditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

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
}

@Composable
fun ActionEditorScreen(
    actionId: Long?,
    cadence: RoutineCadence,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActionEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState(actionId).collectAsStateWithLifecycle(
        initialValue = ActionEditorUiState(isEditing = actionId != null),
    )

    ActionEditorComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                ActionEditorUiEvent.BackClick -> onBackClick()
                is ActionEditorUiEvent.DescriptionChange -> viewModel.updateDescription(event.description)
                is ActionEditorUiEvent.RepeatEnabledChange -> viewModel.updateRepeatEnabled(event.enabled)
                is ActionEditorUiEvent.RepeatTargetCountChange -> viewModel.updateRepeatTargetCount(event.targetCount)
                ActionEditorUiEvent.SaveClick -> {
                    viewModel.save(
                        actionId = actionId,
                        cadence = cadence,
                        onSaved = onBackClick,
                    )
                }
                is ActionEditorUiEvent.TitleChange -> viewModel.updateTitle(event.title)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionEditorComponent(
    uiState: ActionEditorUiState,
    onEvent: (ActionEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(ActionEditorUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(text = if (uiState.isEditing) "Edit action" else "New action")
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { title -> onEvent(ActionEditorUiEvent.TitleChange(title)) },
                label = { Text(text = "Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { description -> onEvent(ActionEditorUiEvent.DescriptionChange(description)) },
                label = { Text(text = "Description") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            RepeatCountEditor(
                uiState = uiState,
                onEvent = onEvent,
            )
            Button(
                onClick = { onEvent(ActionEditorUiEvent.SaveClick) },
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Save")
            }
        }
    }
}

@Composable
private fun RepeatCountEditor(
    uiState: ActionEditorUiState,
    onEvent: (ActionEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Repeat count")
                Text(
                    text = "Use a counter instead of a checkbox",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = uiState.isRepeatEnabled,
                onCheckedChange = { enabled -> onEvent(ActionEditorUiEvent.RepeatEnabledChange(enabled)) },
            )
        }
        if (uiState.isRepeatEnabled) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    enabled = uiState.repeatTargetCount > 2,
                    onClick = {
                        onEvent(ActionEditorUiEvent.RepeatTargetCountChange(uiState.repeatTargetCount - 1))
                    },
                ) {
                    Text(text = "-")
                }
                Text(
                    text = "${uiState.repeatTargetCount} times",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
                TextButton(
                    onClick = {
                        onEvent(ActionEditorUiEvent.RepeatTargetCountChange(uiState.repeatTargetCount + 1))
                    },
                ) {
                    Text(text = "+")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionEditorComponentPreview() {
    RoutineHelperTheme {
        ActionEditorComponent(
            uiState = ActionEditorUiState.preview(),
            onEvent = {},
        )
    }
}
