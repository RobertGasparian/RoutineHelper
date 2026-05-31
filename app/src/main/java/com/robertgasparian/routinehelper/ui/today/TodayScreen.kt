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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import com.robertgasparian.routinehelper.BuildConfig
import com.robertgasparian.routinehelper.ui.dsm.NoteEditorDialog
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface TodayUiEvent {
    data object CreateActionClick : TodayUiEvent

    data class EditActionClick(
        val actionId: Long,
    ) : TodayUiEvent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : TodayUiEvent

    data class CompletedCountChange(
        val routineItemId: Long,
        val completedCount: Int,
    ) : TodayUiEvent

    data class NoteChange(
        val routineItemId: Long,
        val note: String,
    ) : TodayUiEvent

    data class SummaryNoteChange(
        val note: String,
    ) : TodayUiEvent

    data object SnapshotClick : TodayUiEvent
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
                is TodayUiEvent.CompletedCountChange -> viewModel.updateCompletedCount(
                    routineItemId = event.routineItemId,
                    completedCount = event.completedCount,
                )
                TodayUiEvent.CreateActionClick -> onCreateActionClick()
                is TodayUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                is TodayUiEvent.NoteChange -> viewModel.updateNote(
                    routineItemId = event.routineItemId,
                    note = event.note,
                )
                is TodayUiEvent.SummaryNoteChange -> viewModel.updateSummaryNote(event.note)
                TodayUiEvent.SnapshotClick -> viewModel.snapshotToday()
            }
        },
        showSnapshotAction = BuildConfig.DEBUG,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayComponent(
    uiState: TodayUiState,
    onEvent: (TodayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Today",
    emptyTitle: String = "No routine items yet",
    emptyDescription: String = "Add your first action to start tracking today.",
    showSnapshotAction: Boolean = true,
) {
    var noteEditorItem by rememberSaveable { mutableStateOf<TodayItemUiState?>(null) }
    var isSummaryNoteEditorVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title)
                        Text(
                            text = uiState.date,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    if (showSnapshotAction) {
                        TextButton(
                            onClick = {
                                // TODO Remove this debug-only action when worker triggering has a dedicated test tool.
                                onEvent(TodayUiEvent.SnapshotClick)
                            },
                        ) {
                            Text(text = "Snapshot")
                        }
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
                title = emptyTitle,
                description = emptyDescription,
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
                item {
                    SummaryNoteCard(
                        note = uiState.summaryNote,
                        label = if (title == "Weekly") "Week note" else "Day note",
                        onEditClick = { isSummaryNoteEditorVisible = true },
                    )
                }
                items(
                    items = uiState.items,
                    key = { item -> item.routineItemId },
                ) { item ->
                    TodayItemCard(
                        item = item,
                        onEvent = { itemEvent ->
                            when (itemEvent) {
                                is TodayItemCardUiEvent.CheckedChange -> {
                                    onEvent(TodayUiEvent.CheckedChange(item.routineItemId, itemEvent.isChecked))
                                }
                                is TodayItemCardUiEvent.CompletedCountChange -> {
                                    onEvent(TodayUiEvent.CompletedCountChange(item.routineItemId, itemEvent.completedCount))
                                }
                                TodayItemCardUiEvent.EditActionClick -> {
                                    onEvent(TodayUiEvent.EditActionClick(item.actionId))
                                }
                                TodayItemCardUiEvent.EditNoteClick -> {
                                    noteEditorItem = item
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    noteEditorItem?.let { item ->
        NoteEditorDialog(
            title = if (item.note.isBlank()) "Add note" else "Edit note",
            textFieldLabel = "Today note",
            initialNote = item.note,
            onDismiss = { noteEditorItem = null },
            onConfirm = { note ->
                onEvent(TodayUiEvent.NoteChange(item.routineItemId, note))
                noteEditorItem = null
            },
        )
    }

    if (isSummaryNoteEditorVisible) {
        val label = if (title == "Weekly") "Week note" else "Day note"
        NoteEditorDialog(
            title = label,
            textFieldLabel = label,
            initialNote = uiState.summaryNote,
            onDismiss = { isSummaryNoteEditorVisible = false },
            onConfirm = { note ->
                onEvent(TodayUiEvent.SummaryNoteChange(note))
                isSummaryNoteEditorVisible = false
            },
        )
    }
}

@Composable
private fun EmptyTodayContent(
    onAddClick: () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = description,
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

private sealed interface TodayItemCardUiEvent {
    data object EditActionClick : TodayItemCardUiEvent

    data object EditNoteClick : TodayItemCardUiEvent

    data class CheckedChange(
        val isChecked: Boolean,
    ) : TodayItemCardUiEvent

    data class CompletedCountChange(
        val completedCount: Int,
    ) : TodayItemCardUiEvent
}

@Composable
private fun TodayItemCard(
    item: TodayItemUiState,
    onEvent: (TodayItemCardUiEvent) -> Unit,
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
                if (item.isRepeatAction) {
                    RepeatCountControl(
                        completedCount = item.completedCount,
                        repeatTargetCount = item.repeatTargetCount ?: 2,
                        onCompletedCountChange = { completedCount ->
                            onEvent(TodayItemCardUiEvent.CompletedCountChange(completedCount))
                        },
                    )
                } else {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { isChecked ->
                            onEvent(TodayItemCardUiEvent.CheckedChange(isChecked))
                        },
                    )
                }
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
                TextButton(onClick = { onEvent(TodayItemCardUiEvent.EditActionClick) }) {
                    Text(text = "Edit action")
                }
                TextButton(onClick = { onEvent(TodayItemCardUiEvent.EditNoteClick) }) {
                    Text(text = if (item.note.isBlank()) "Add note" else "Edit note")
                }
            }
        }
    }
}

@Composable
private fun SummaryNoteCard(
    note: String,
    label: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = note.takeIf(String::isNotBlank) ?: "No summary note yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            TextButton(onClick = onEditClick) {
                Text(text = if (note.isBlank()) "Add" else "Edit")
            }
        }
    }
}

@Composable
private fun RepeatCountControl(
    completedCount: Int,
    repeatTargetCount: Int,
    onCompletedCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(
            enabled = completedCount > 0,
            onClick = { onCompletedCountChange((completedCount - 1).coerceAtLeast(0)) },
        ) {
            Text(text = "-")
        }
        Text(
            text = "${completedCount.coerceIn(0, repeatTargetCount)}/$repeatTargetCount",
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(
            enabled = completedCount < repeatTargetCount,
            onClick = { onCompletedCountChange((completedCount + 1).coerceAtMost(repeatTargetCount)) },
        ) {
            Text(text = "+")
        }
    }
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
