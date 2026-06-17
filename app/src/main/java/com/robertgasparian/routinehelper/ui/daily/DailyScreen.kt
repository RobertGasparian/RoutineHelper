package com.robertgasparian.routinehelper.ui.daily

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.BuildConfig
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDialog
import com.robertgasparian.routinehelper.ui.dsm.SummaryNoteCard
import com.robertgasparian.routinehelper.ui.dsm.RoutineActionItemCard
import com.robertgasparian.routinehelper.ui.reorder.RoutineReorderList
import com.robertgasparian.routinehelper.ui.reorder.rememberRoutineReorderState
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DailyScreen(
    onCreateActionClick: () -> Unit,
    onEditActionClick: (actionId: Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DailyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                DailyUiEvent.CreateActionClick -> onCreateActionClick()
                is DailyUiEvent.EditActionClick -> onEditActionClick(event.actionId)
                is DailyUiEvent.Intent -> viewModel.onEvent(event)
            }
        },
        showSnapshotAction = BuildConfig.DEBUG,
        onSettingsClick = onSettingsClick,
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
    onSettingsClick: () -> Unit = {},
) {
    // TODO Remove this debug/test-only date picker before the first public release.
    var isSnapshotDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val reorderState = rememberRoutineReorderState()
    val fabVisible by remember {
        derivedStateOf {
            !listState.isScrollInProgress
        }
    }
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "DailyFabScale",
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
                                isSnapshotDatePickerVisible = true
                            },
                        ) {
                            Text(text = "Snapshot")
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .padding(bottom = DailyFabBottomClearance)
                    .scale(fabScale),
                onClick = {
                    if (fabVisible) {
                        onEvent(DailyUiEvent.CreateActionClick)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add action",
                )
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
            val contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = DailyListBottomSafeSpace,
            )
            val summaryContent: @Composable () -> Unit = {
                SummaryNoteCard(
                    note = uiState.summaryNote,
                    label = if (title == "Weekly") "Week note" else "Day note",
                    onEditClick = {
                        onEvent(DailyUiEvent.EditSummaryNoteClick)
                    },
                )
            }
            val itemContent:
                @Composable (DailyItemUiState, Modifier, Modifier) -> Unit = { item, itemModifier, dragHandleModifier ->
                    DailyRoutineItem(
                        item = item,
                        onEvent = onEvent,
                        modifier = itemModifier,
                        dragHandleModifier = dragHandleModifier,
                    )
                }

            RoutineReorderList(
                sourceItems = uiState.items,
                reorderState = reorderState,
                listState = listState,
                coroutineScope = coroutineScope,
                contentPadding = contentPadding,
                summaryContent = summaryContent,
                onDropOrder = { orderedIds -> onEvent(DailyUiEvent.ReorderItems(orderedIds)) },
                itemContent = itemContent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    uiState.noteEditor?.let { editor ->
        RoutineNoteDialog(
            value = editor.value.toTextFieldValue(),
            onValueChange = { value -> onEvent(DailyUiEvent.NoteDraftChange(value.toNoteDraftUiState())) },
            title = editor.title,
            supportingText = editor.supportingText,
            label = editor.label,
            onDismiss = { onEvent(DailyUiEvent.NoteEditorDismiss) },
            onSaveClick = { onEvent(DailyUiEvent.NoteEditorSaveClick) },
            onClearClick = { onEvent(DailyUiEvent.NoteDraftClearClick) },
            onDateClick = { onEvent(DailyUiEvent.NoteDraftDateClick) },
            onWeekdayClick = { onEvent(DailyUiEvent.NoteDraftWeekdayClick) },
            onTimeClick = { onEvent(DailyUiEvent.NoteDraftTimeClick) },
        )
    }

    if (showSnapshotAction && isSnapshotDatePickerVisible) {
        DebugSnapshotDatePickerDialog(
            onDismiss = { isSnapshotDatePickerVisible = false },
            onDateSelected = { date ->
                isSnapshotDatePickerVisible = false
                onEvent(DailyUiEvent.SnapshotDateSelected(date))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugSnapshotDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    // TODO Remove this debug/test-only date picker before the first public release.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now().toUtcStartOfDayMillis(),
        selectableDates = object : SelectableDates {},
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDateString()
                        ?.let(onDateSelected)
                },
            ) {
                Text(text = "Snapshot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Snapshot date",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                )
            },
            headline = {
                Text(
                    text = "Test snapshot target",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                )
            },
        )
    }
}

@Composable
private fun DailyRoutineItem(
    item: DailyItemUiState,
    onEvent: (DailyUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    RoutineActionItemCard(
        modifier = modifier,
        title = item.title,
        description = item.description,
        note = item.note.takeIf(String::isNotBlank),
        isChecked = item.isChecked,
        isHidden = item.isHidden,
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
            onEvent(DailyUiEvent.EditNoteClick(item))
        },
        onHiddenChange = { isHidden ->
            onEvent(DailyUiEvent.HiddenChange(item.routineItemId, isHidden))
        },
        dragHandleModifier = dragHandleModifier,
    )
}

private val DailyListBottomSafeSpace = 128.dp
private val DailyFabBottomClearance = 78.dp

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDateString(): String =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()

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
