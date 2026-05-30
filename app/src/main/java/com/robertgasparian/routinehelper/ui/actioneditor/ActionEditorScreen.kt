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
import androidx.compose.material3.Text
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
