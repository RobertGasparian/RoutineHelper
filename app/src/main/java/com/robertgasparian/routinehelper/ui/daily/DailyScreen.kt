package com.robertgasparian.routinehelper.ui.daily

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.BuildConfig
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDialog
import com.robertgasparian.routinehelper.ui.dsm.SummaryNoteCard
import com.robertgasparian.routinehelper.ui.dsm.RoutineActionItemCard
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface DailyUiEvent {
    data object CreateActionClick : DailyUiEvent

    data class EditActionClick(
        val actionId: Long,
    ) : DailyUiEvent

    data class CheckedChange(
        val routineItemId: Long,
        val isChecked: Boolean,
    ) : DailyUiEvent

    data class CompletedCountChange(
        val routineItemId: Long,
        val completedCount: Int,
    ) : DailyUiEvent

    data class NoteChange(
        val routineItemId: Long,
        val note: String,
    ) : DailyUiEvent

    data class SummaryNoteChange(
        val note: String,
    ) : DailyUiEvent

    data object SnapshotClick : DailyUiEvent
}

@Composable
fun DailyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    viewModel: DailyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is DailyUiEvent.CheckedChange -> viewModel.setChecked(
                    routineItemId = event.routineItemId,
                    isChecked = event.isChecked,
                )
                is DailyUiEvent.CompletedCountChange -> viewModel.updateCompletedCount(
                    routineItemId = event.routineItemId,
                    completedCount = event.completedCount,
                )
                DailyUiEvent.CreateActionClick -> onCreateActionClick()
                is DailyUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                is DailyUiEvent.NoteChange -> viewModel.updateNote(
                    routineItemId = event.routineItemId,
                    note = event.note,
                )
                is DailyUiEvent.SummaryNoteChange -> viewModel.updateSummaryNote(event.note)
                DailyUiEvent.SnapshotClick -> viewModel.snapshotDaily()
            }
        },
        showSnapshotAction = BuildConfig.DEBUG,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyComponent(
    uiState: DailyUiState,
    onEvent: (DailyUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Daily",
    emptyTitle: String = "No routine items yet",
    emptyDescription: String = "Add your first daily action to start tracking.",
    showSnapshotAction: Boolean = true,
) {
    var noteEditorItem by rememberSaveable { mutableStateOf<DailyItemUiState?>(null) }
    var isSummaryNoteEditorVisible by rememberSaveable { mutableStateOf(false) }
    var noteEditorText by rememberSaveable { mutableStateOf("") }
    var summaryNoteEditorText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 32
        }
    }
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        label = "DailyFabScale",
    )
    val fabAlpha by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        label = "DailyFabAlpha",
    )

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
                                onEvent(DailyUiEvent.SnapshotClick)
                            },
                        ) {
                            Text(text = "Snapshot")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (fabScale > 0.01f) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(bottom = DailyFabBottomClearance)
                        .graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                            alpha = fabAlpha
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 1f)
                        },
                    onClick = { onEvent(DailyUiEvent.CreateActionClick) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add action",
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            EmptyDailyContent(
                onAddClick = { onEvent(DailyUiEvent.CreateActionClick) },
                title = emptyTitle,
                description = emptyDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = DailyListBottomSafeSpace,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SummaryNoteCard(
                        note = uiState.summaryNote,
                        label = if (title == "Weekly") "Week note" else "Day note",
                        onEditClick = {
                            summaryNoteEditorText = uiState.summaryNote
                            isSummaryNoteEditorVisible = true
                        },
                    )
                }
                items(
                    items = uiState.items,
                    key = { item -> item.routineItemId },
                ) { item ->
                    RoutineActionItemCard(
                        title = item.title,
                        description = item.description,
                        note = item.note.takeIf(String::isNotBlank),
                        isChecked = item.isChecked,
                        repeatTargetCount = item.repeatTargetCount,
                        completedCount = item.completedCount,
                        onCheckedChange = { isChecked ->
                            onEvent(DailyUiEvent.CheckedChange(item.routineItemId, isChecked))
                        },
                        onCompletedCountChange = { completedCount ->
                            onEvent(DailyUiEvent.CompletedCountChange(item.routineItemId, completedCount))
                        },
                        onEditActionClick = {
                            onEvent(DailyUiEvent.EditActionClick(item.actionId))
                        },
                        onEditNoteClick = {
                            noteEditorItem = item
                            noteEditorText = item.note
                        },
                    )
                }
            }
        }
    }

    noteEditorItem?.let { item ->
        val itemNoteLabel = if (title == "Weekly") "Weekly note" else "Daily note"
        RoutineNoteDialog(
            note = noteEditorText,
            onNoteChange = { noteEditorText = it },
            title = if (item.note.isBlank()) "Add note" else "Edit note",
            supportingText = "$itemNoteLabel for ${item.title}",
            placeholder = itemNoteLabel,
            onDismiss = { noteEditorItem = null },
            onSaveClick = {
                onEvent(DailyUiEvent.NoteChange(item.routineItemId, noteEditorText))
                noteEditorItem = null
            },
        )
    }

    if (isSummaryNoteEditorVisible) {
        val label = if (title == "Weekly") "Week note" else "Day note"
        RoutineNoteDialog(
            note = summaryNoteEditorText,
            onNoteChange = { summaryNoteEditorText = it },
            title = label,
            supportingText = if (title == "Weekly") {
                "This note is saved for the current week."
            } else {
                "This note is saved for this day only."
            },
            placeholder = label,
            onDismiss = { isSummaryNoteEditorVisible = false },
            onSaveClick = {
                onEvent(DailyUiEvent.SummaryNoteChange(summaryNoteEditorText))
                isSummaryNoteEditorVisible = false
            },
        )
    }
}

private val DailyListBottomSafeSpace = 128.dp
private val DailyFabBottomClearance = 78.dp

@Composable
private fun EmptyDailyContent(
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

@Preview(showBackground = true)
@Composable
private fun DailyScreenPreview() {
    RoutineHelperTheme {
        DailyComponent(
            uiState = DailyUiState.preview(),
            onEvent = {},
        )
    }
}
