package com.robertgasparian.routinehelper.ui.tracking

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.routinetracking.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineActionItemCard
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDialog
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDialogIntent
import com.robertgasparian.routinehelper.ui.dsm.RoutineReorderableLazyColumn
import com.robertgasparian.routinehelper.ui.dsm.RoutineReorderDragStartMode
import com.robertgasparian.routinehelper.ui.dsm.RoutineSwipeToReveal
import com.robertgasparian.routinehelper.ui.dsm.SummaryNoteCard
import com.robertgasparian.routinehelper.ui.dsm.rememberLazyListIsActuallyScrolling
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineTrackingComponent(
    uiState: RoutineTrackingUiState,
    onIntent: (RoutineTrackingIntent) -> Unit,
    modifier: Modifier = Modifier,
    cadence: RoutineCadence = RoutineCadence.Daily,
    showSnapshotAction: Boolean = false,
    showAddTestItems: Boolean = false,
) {
    // TODO Remove this debug/test-only date picker before the first public release.
    var isSnapshotDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    var revealedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val isWeekly = cadence == RoutineCadence.Weekly
    val title = stringResource(
        if (isWeekly) R.string.routine_tracking_weekly_title else R.string.routine_tracking_daily_title,
    )
    val date = if (isWeekly) {
        stringResource(R.string.routine_tracking_week_of, uiState.date)
    } else {
        uiState.date
    }
    val emptyTitle = stringResource(
        if (isWeekly) {
            R.string.routine_tracking_weekly_empty_title
        } else {
            R.string.routine_tracking_daily_empty_title
        },
    )
    val emptyDescription = stringResource(
        if (isWeekly) {
            R.string.routine_tracking_weekly_empty_description
        } else {
            R.string.routine_tracking_daily_empty_description
        },
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { isScrolling -> isScrolling }
            .collect { revealedItemId = null }
    }

    LaunchedEffect(uiState.canRemoveItems) {
        if (!uiState.canRemoveItems) {
            revealedItemId = null
        }
    }

    val fabVisible = !rememberLazyListIsActuallyScrolling(listState)
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
                            text = date,
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
                            Text(text = stringResource(R.string.routine_tracking_snapshot))
                        }
                    }
                    IconButton(onClick = { onIntent(RoutineTrackingIntent.SettingsClick) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.routine_tracking_open_settings),
                        )
                    }
                    if (showAddTestItems) {
                        RoutineTrackingDebugOverflowMenu(
                            onAddTestItemsClick = {
                                onIntent(RoutineTrackingIntent.AddTestItemsClick)
                            },
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
                        onIntent(RoutineTrackingIntent.CreateActionClick)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.routine_tracking_add_action),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            EmptyRoutineTrackingContent(
                onAddClick = { onIntent(RoutineTrackingIntent.CreateActionClick) },
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
                    label = stringResource(
                        if (isWeekly) R.string.routine_tracking_week_note else R.string.routine_tracking_day_note,
                    ),
                    onEditClick = {
                        onIntent(RoutineTrackingIntent.EditSummaryNoteClick)
                    },
                )
            }
            RoutineReorderableLazyColumn(
                items = uiState.items,
                itemId = RoutineTrackingItemUiState::routineItemId,
                state = listState,
                contentPadding = contentPadding,
                itemSpacing = 12.dp,
                dragStartMode = RoutineReorderDragStartMode.LongPress,
                header = summaryContent,
                onOrderChange = { orderedIds -> onIntent(RoutineTrackingIntent.ReorderItems(orderedIds)) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) { item ->
                LaunchedEffect(isDragHandleActive) {
                    if (isDragHandleActive && revealedItemId == item.routineItemId) {
                        revealedItemId = null
                    }
                }
                RoutineSwipeToReveal(
                    isRevealed = revealedItemId == item.routineItemId && !isDragging,
                    enabled = uiState.canRemoveItems && !isDragging,
                    onRevealedChange = { isRevealed ->
                        if (isRevealed) {
                            revealedItemId = item.routineItemId
                        } else if (revealedItemId == item.routineItemId) {
                            revealedItemId = null
                        }
                    },
                    onAction = {
                        onIntent(RoutineTrackingIntent.RemoveItem(item.routineItemId))
                    },
                    backgroundContent = {
                        RoutineTrackingDeleteBackground()
                    },
                    actionContent = { onDeleteClick ->
                        RoutineTrackingDeleteAction(
                            itemTitle = item.title,
                            onClick = onDeleteClick,
                        )
                    },
                ) {
                    RoutineTrackingItem(
                        item = item,
                        isDragging = isDragging,
                        onIntent = onIntent,
                        modifier = dragHandleModifier,
                    )
                }
            }
        }
    }

    uiState.noteEditor?.let { editor ->
        val isItemEditor = editor.target is NoteEditorTarget.Item
        val editorLabel = stringResource(
            when {
                isItemEditor && editor.cadence == RoutineCadence.Weekly -> R.string.routine_tracking_weekly_note
                isItemEditor -> R.string.routine_tracking_daily_note
                editor.cadence == RoutineCadence.Weekly -> R.string.routine_tracking_week_note
                else -> R.string.routine_tracking_day_note
            },
        )
        val editorTitle = if (isItemEditor) {
            stringResource(
                if (editor.value.text.isBlank()) {
                    R.string.routine_tracking_add_note
                } else {
                    R.string.routine_tracking_edit_note
                },
            )
        } else {
            editorLabel
        }
        val editorSupportingText = if (isItemEditor) {
            stringResource(
                R.string.routine_tracking_item_note_supporting_text,
                editorLabel,
                editor.itemTitle.orEmpty(),
            )
        } else {
            stringResource(
                if (editor.cadence == RoutineCadence.Weekly) {
                    R.string.routine_tracking_week_note_supporting_text
                } else {
                    R.string.routine_tracking_day_note_supporting_text
                },
            )
        }
        RoutineNoteDialog(
            value = editor.value.toTextFieldValue(),
            onIntent = { dialogIntent ->
                when (dialogIntent) {
                    is RoutineNoteDialogIntent.ValueChange -> {
                        onIntent(dialogIntent.value.toNoteDraftChange())
                    }
                    RoutineNoteDialogIntent.Dismiss -> onIntent(RoutineTrackingIntent.NoteEditorDismiss)
                    RoutineNoteDialogIntent.SaveClick -> onIntent(RoutineTrackingIntent.NoteEditorSaveClick)
                    RoutineNoteDialogIntent.ClearClick -> onIntent(RoutineTrackingIntent.NoteDraftClearClick)
                    RoutineNoteDialogIntent.DateClick -> onIntent(RoutineTrackingIntent.NoteDraftDateClick)
                    RoutineNoteDialogIntent.WeekdayClick -> onIntent(RoutineTrackingIntent.NoteDraftWeekdayClick)
                    RoutineNoteDialogIntent.TimeClick -> onIntent(RoutineTrackingIntent.NoteDraftTimeClick)
                }
            },
            title = editorTitle,
            supportingText = editorSupportingText,
            label = editorLabel,
        )
    }

    if (showSnapshotAction && isSnapshotDatePickerVisible) {
        DebugSnapshotDatePickerDialog(
            onDismiss = { isSnapshotDatePickerVisible = false },
            onDateSelected = { date ->
                isSnapshotDatePickerVisible = false
                onIntent(RoutineTrackingIntent.SnapshotDateSelected(date))
            },
        )
    }
}

@Composable
private fun RoutineTrackingDebugOverflowMenu(
    onAddTestItemsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.routine_tracking_more_options),
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.routine_tracking_add_test_items)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                onClick = {
                    isExpanded = false
                    onAddTestItemsClick()
                },
            )
        }
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
                Text(text = stringResource(R.string.routine_tracking_snapshot))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.routine_tracking_cancel))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = stringResource(R.string.routine_tracking_snapshot_date),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                )
            },
            headline = {
                Text(
                    text = stringResource(R.string.routine_tracking_test_snapshot_target),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                )
            },
        )
    }
}

@Composable
private fun RoutineTrackingItem(
    item: RoutineTrackingItemUiState,
    isDragging: Boolean,
    onIntent: (RoutineTrackingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoutineActionItemCard(
        modifier = modifier,
        title = item.title,
        description = item.description,
        note = item.note.takeIf(String::isNotBlank),
        isChecked = item.isChecked,
        isHidden = item.isHidden,
        isDragging = isDragging,
        repeatTargetCount = item.repeatTargetCount,
        completedCount = item.completedCount,
        onCheckedChange = { isChecked ->
            onIntent(RoutineTrackingIntent.CheckedChange(item.routineItemId, isChecked))
        },
        onCompletedCountChange = { completedCount ->
            onIntent(RoutineTrackingIntent.CompletedCountChange(item.routineItemId, completedCount))
        },
        onEditActionClick = {
            onIntent(RoutineTrackingIntent.EditActionClick(item.actionId))
        },
        onEditNoteClick = {
            onIntent(
                RoutineTrackingIntent.EditNoteClick(
                    routineItemId = item.routineItemId,
                    itemTitle = item.title,
                    note = item.note,
                ),
            )
        },
        onHiddenChange = { isHidden ->
            onIntent(RoutineTrackingIntent.HiddenChange(item.routineItemId, isHidden))
        },
    )
}

@Composable
private fun RoutineTrackingDeleteBackground(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {}
}

@Composable
private fun RoutineTrackingDeleteAction(
    itemTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(
            topEnd = 16.dp,
            bottomEnd = 16.dp,
        ),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.routine_tracking_delete_action, itemTitle),
            )
        }
    }
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
            Text(text = stringResource(R.string.routine_tracking_add_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineTrackingComponentPreview() {
    RoutineHelperTheme {
        RoutineTrackingComponent(
            uiState = RoutineTrackingUiState.preview(),
            onIntent = {},
            showSnapshotAction = true,
        )
    }
}
