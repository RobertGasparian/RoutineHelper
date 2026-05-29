package com.robertgasparian.routinehelper.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayComponent(
        uiState = uiState,
        onAddAction = viewModel::addAction,
        onCheckedChange = viewModel::setChecked,
        onNoteChange = viewModel::updateNote,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayComponent(
    uiState: TodayUiState,
    onAddAction: (title: String, description: String?) -> Unit,
    onCheckedChange: (routineItemId: Long, isChecked: Boolean) -> Unit,
    onNoteChange: (routineItemId: Long, note: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

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
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
            ) {
                Text(text = "+")
            }
        },
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            EmptyTodayContent(
                onAddClick = { showAddDialog = true },
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
                        onCheckedChange = { isChecked ->
                            onCheckedChange(item.routineItemId, isChecked)
                        },
                        onNoteChange = { note ->
                            onNoteChange(item.routineItemId, note)
                        },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddActionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description ->
                onAddAction(title, description)
                showAddDialog = false
            },
        )
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
    onCheckedChange: (Boolean) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditingNote by rememberSaveable(item.routineItemId) { mutableStateOf(false) }
    var noteDraft by rememberSaveable(item.routineItemId) { mutableStateOf(item.note) }

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

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingNote) {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Today note") },
                    minLines = 2,
                    maxLines = 5,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            noteDraft = item.note
                            isEditingNote = false
                        },
                    ) {
                        Text(text = "Cancel")
                    }
                    TextButton(
                        onClick = {
                            onNoteChange(noteDraft)
                            isEditingNote = false
                        },
                    ) {
                        Text(text = "Save")
                    }
                }
            } else {
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            noteDraft = item.note
                            isEditingNote = true
                        },
                    ) {
                        Text(text = if (item.note.isBlank()) "Add note" else "Edit note")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddActionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val canSave = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add action") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description) },
                enabled = canSave,
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
            onAddAction = { _, _ -> },
            onCheckedChange = { _, _ -> },
            onNoteChange = { _, _ -> },
        )
    }
}
