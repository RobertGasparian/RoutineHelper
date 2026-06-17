package com.robertgasparian.routinehelper.ui.tracking

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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
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
import com.robertgasparian.routinehelper.ui.dsm.RoutineActionItemCard
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDialog
import com.robertgasparian.routinehelper.ui.dsm.SummaryNoteCard
import com.robertgasparian.routinehelper.ui.reorder.RoutineReorderList
import com.robertgasparian.routinehelper.ui.reorder.rememberRoutineReorderState
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTrackingComponent(
    uiState: RoutineTrackingUiState,
    onEvent: (RoutineTrackingUiEvent) -> Unit,
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
        label = "RoutineTrackingFabScale",
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
                    .padding(bottom = RoutineTrackingFabBottomClearance)
                    .scale(fabScale),
                onClick = {
                    if (fabVisible) {
                        onEvent(RoutineTrackingUiEvent.CreateActionClick)
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
            EmptyRoutineTrackingContent(
                onAddClick = { onEvent(RoutineTrackingUiEvent.CreateActionClick) },
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
                bottom = RoutineTrackingListBottomSafeSpace,
            )
            val summaryContent: @Composable () -> Unit = {
                SummaryNoteCard(
                    note = uiState.summaryNote,
                    label = if (title == "Weekly") "Week note" else "Day note",
                    onEditClick = {
                        onEvent(RoutineTrackingUiEvent.EditSummaryNoteClick)
                    },
                )
            }
            val itemContent:
                @Composable (
                    RoutineTrackingItemUiState,
                    Modifier,
                    Modifier,
                ) -> Unit = { item, itemModifier, dragHandleModifier ->
                    RoutineTrackingItem(
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
                onDropOrder = { orderedIds -> onEvent(RoutineTrackingUiEvent.ReorderItems(orderedIds)) },
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
            onValueChange = { value -> onEvent(RoutineTrackingUiEvent.NoteDraftChange(value.toNoteDraftUiState())) },
            title = editor.title,
            supportingText = editor.supportingText,
            label = editor.label,
            onDismiss = { onEvent(RoutineTrackingUiEvent.NoteEditorDismiss) },
            onSaveClick = { onEvent(RoutineTrackingUiEvent.NoteEditorSaveClick) },
            onClearClick = { onEvent(RoutineTrackingUiEvent.NoteDraftClearClick) },
            onDateClick = { onEvent(RoutineTrackingUiEvent.NoteDraftDateClick) },
            onWeekdayClick = { onEvent(RoutineTrackingUiEvent.NoteDraftWeekdayClick) },
            onTimeClick = { onEvent(RoutineTrackingUiEvent.NoteDraftTimeClick) },
        )
    }

    if (showSnapshotAction && isSnapshotDatePickerVisible) {
        DebugSnapshotDatePickerDialog(
            onDismiss = { isSnapshotDatePickerVisible = false },
            onDateSelected = { date ->
                isSnapshotDatePickerVisible = false
                onEvent(RoutineTrackingUiEvent.SnapshotDateSelected(date))
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
private fun RoutineTrackingItem(
    item: RoutineTrackingItemUiState,
    onEvent: (RoutineTrackingUiEvent) -> Unit,
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
            onEvent(RoutineTrackingUiEvent.CheckedChange(item.routineItemId, isChecked))
        },
        onCompletedCountChange = { completedCount ->
            onEvent(RoutineTrackingUiEvent.CompletedCountChange(item.routineItemId, completedCount))
        },
        onEditActionClick = {
            onEvent(RoutineTrackingUiEvent.EditActionClick(item.actionId))
        },
        onEditNoteClick = {
            onEvent(RoutineTrackingUiEvent.EditNoteClick(item))
        },
        onHiddenChange = { isHidden ->
            onEvent(RoutineTrackingUiEvent.HiddenChange(item.routineItemId, isHidden))
        },
        dragHandleModifier = dragHandleModifier,
    )
}

private val RoutineTrackingListBottomSafeSpace = 128.dp
private val RoutineTrackingFabBottomClearance = 78.dp

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDateString(): String =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()

@Composable
private fun EmptyRoutineTrackingContent(
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
private fun RoutineTrackingComponentPreview() {
    RoutineHelperTheme {
        RoutineTrackingComponent(
            uiState = RoutineTrackingUiState.preview(),
            onEvent = {},
        )
    }
}
