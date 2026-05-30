package com.robertgasparian.routinehelper.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

sealed interface TodayUiEvent {
    data object CreateActionClick : TodayUiEvent

    data class EditActionClick(
        val actionId: Long,
    ) : TodayUiEvent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : TodayUiEvent

    data class NoteChange(
        val routineItemId: Long,
        val note: String,
    ) : TodayUiEvent

    data class SnapshotClick(
        val snapshotDate: String,
    ) : TodayUiEvent
}

@Composable
fun TodayScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
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
                is TodayUiEvent.SnapshotClick -> viewModel.snapshotToday(event.snapshotDate)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayComponent(
    uiState: TodayUiState,
    onEvent: (TodayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var noteEditorItem by rememberSaveable { mutableStateOf<TodayItemUiState?>(null) }
    var showSnapshotDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Today")
                        Text(
                            text = uiState.date,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // TODO Remove this test-only date picker when snapshots are created by WorkManager.
                            showSnapshotDatePicker = true
                        },
                    ) {
                        Text(text = "Snapshot")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TodayUiEvent.CreateActionClick) },
            ) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            EmptyTodayContent(
                onAddClick = { onEvent(TodayUiEvent.CreateActionClick) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.items,
                    key = { item -> item.routineItemId },
                ) { item ->
                    TodayItemCard(
                        item = item,
                        onEditActionClick = {
                            onEvent(TodayUiEvent.EditActionClick(item.actionId))
                        },
                        onCheckedChange = { isChecked ->
                            onEvent(TodayUiEvent.CheckedChange(item.routineItemId, isChecked))
                        },
                        onEditNoteClick = {
                            noteEditorItem = item
                        },
                    )
                }
            }
        }
    }

    if (showSnapshotDatePicker) {
        SnapshotDatePickerDialog(
            initialDate = uiState.date,
            onDismiss = { showSnapshotDatePicker = false },
            onConfirm = { snapshotDate ->
                onEvent(TodayUiEvent.SnapshotClick(snapshotDate))
                showSnapshotDatePicker = false
            },
        )
    }

    noteEditorItem?.let { item ->
        NoteEditorDialog(
            initialNote = item.note,
            onDismiss = { noteEditorItem = null },
            onConfirm = { note ->
                onEvent(TodayUiEvent.NoteChange(item.routineItemId, note))
                noteEditorItem = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnapshotDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                        ?.toLocalDateString()
                        ?: initialDate
                    onConfirm(selectedDate)
                },
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun EmptyTodayContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No routine items yet",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Add your first action to start tracking today.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(text = "Add action")
        }
    }
}

@Composable
private fun TodayItemCard(
    item: TodayItemUiState,
    onEditActionClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onEditNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = onCheckedChange,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (item.isChecked) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        },
                    )
                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onEditActionClick) {
                    Text(text = "Edit action")
                }
                TextButton(onClick = onEditNoteClick) {
                    Text(text = if (item.note.isBlank()) "Add note" else "Edit note")
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (note: String) -> Unit,
) {
    var note by rememberSaveable(initialNote) { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initialNote.isBlank()) "Add note" else "Edit note") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = "Today note") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(note) },
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    RoutineHelperTheme {
        TodayComponent(
            uiState = TodayUiState.preview(),
            onEvent = {},
        )
    }
}

private fun String.toEpochMillis(): Long =
    LocalDate.parse(this)
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

private fun Long.toLocalDateString(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()
