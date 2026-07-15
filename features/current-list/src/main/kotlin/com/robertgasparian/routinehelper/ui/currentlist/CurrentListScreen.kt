package com.robertgasparian.routinehelper.ui.currentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.features.currentlist.BuildConfig
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogFilledButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.dsm.RoutineReorderableLazyColumn
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import kotlinx.coroutines.launch

@Composable
fun CurrentListScreen(
    onShareTextPreviewClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CurrentListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CurrentListComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                CurrentListIntent.SettingsClick -> onSettingsClick()
                CurrentListIntent.ShareClick -> {
                    if (uiState.canShare) {
                        onShareTextPreviewClick(uiState.shareText)
                    }
                }
                else -> viewModel.onIntent(intent)
            }
        },
        showAddTestItems = BuildConfig.DEBUG,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentListComponent(
    uiState: CurrentListUiState,
    onIntent: (CurrentListIntent) -> Unit,
    modifier: Modifier = Modifier,
    showAddTestItems: Boolean = false,
) {
    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isOverflowMenuVisible by rememberSaveable { mutableStateOf(false) }
    var isClearConfirmationVisible by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Current List") },
                actions = {
                    IconButton(onClick = { onIntent(CurrentListIntent.SettingsClick) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings",
                        )
                    }
                    Box {
                        IconButton(onClick = { isOverflowMenuVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                            )
                        }
                        CurrentListOverflowMenu(
                            expanded = isOverflowMenuVisible,
                            showAddTestItems = showAddTestItems,
                            canAddTestItems = !uiState.hasItems,
                            canShare = uiState.canShare,
                            canClear = uiState.hasItems,
                            onIntent = { menuIntent ->
                                when (menuIntent) {
                                    CurrentListOverflowMenuIntent.Dismiss -> {
                                        isOverflowMenuVisible = false
                                    }
                                    CurrentListOverflowMenuIntent.AddTestItemsClick -> {
                                        isOverflowMenuVisible = false
                                        onIntent(CurrentListIntent.AddTestItemsClick)
                                    }
                                    CurrentListOverflowMenuIntent.ShareClick -> {
                                        isOverflowMenuVisible = false
                                        onIntent(CurrentListIntent.ShareClick)
                                    }
                                    CurrentListOverflowMenuIntent.ClearClick -> {
                                        isOverflowMenuVisible = false
                                        isClearConfirmationVisible = true
                                    }
                                }
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = CurrentListFabBottomClearance),
                onClick = { isAddDialogVisible = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add item",
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            EmptyCurrentListContent(
                onAddClick = { isAddDialogVisible = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            RoutineReorderableLazyColumn(
                items = uiState.items,
                itemId = CurrentListItemUiState::id,
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = CurrentListBottomSafeSpace,
                ),
                itemSpacing = 12.dp,
                onOrderChange = { orderedIds -> onIntent(CurrentListIntent.ReorderItems(orderedIds)) },
                header = if (uiState.canShowBulkActions) {
                    {
                        CurrentListBulkActionsHeader(
                            canCheckAll = uiState.canCheckAll,
                            canUncheckAll = uiState.canUncheckAll,
                            onCheckAllClick = { onIntent(CurrentListIntent.SetAllChecked(true)) },
                            onUncheckAllClick = { onIntent(CurrentListIntent.SetAllChecked(false)) },
                        )
                    }
                } else {
                    null
                },
                stickyHeader = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) { item ->
                SwipeToRemoveCurrentListItem(
                    item = item,
                    onRemove = { onIntent(CurrentListIntent.RemoveItem(item.id)) },
                ) {
                    CurrentListItemCard(
                        item = item,
                        isDragHandleActive = isDragHandleActive,
                        onCheckedChange = { isChecked ->
                            onIntent(
                                CurrentListIntent.CheckedChange(
                                    itemId = item.id,
                                    isChecked = isChecked,
                                ),
                            )
                        },
                        dragHandleModifier = dragHandleModifier,
                    )
                }
            }
        }
    }

    if (isAddDialogVisible) {
        AddCurrentListItemDialog(
            onDismiss = { isAddDialogVisible = false },
            onAddClick = { title, description ->
                isAddDialogVisible = false
                onIntent(
                    CurrentListIntent.AddItem(
                        title = title,
                        description = description,
                    ),
                )
            },
        )
    }

    if (isClearConfirmationVisible) {
        ClearCurrentListConfirmationDialog(
            onDismiss = { isClearConfirmationVisible = false },
            onClearClick = {
                isClearConfirmationVisible = false
                onIntent(CurrentListIntent.ClearListConfirm)
            },
        )
    }
}

@Composable
private fun CurrentListBulkActionsHeader(
    canCheckAll: Boolean,
    canUncheckAll: Boolean,
    onCheckAllClick: () -> Unit,
    onUncheckAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                CurrentListBulkActionButton(
                    visible = canCheckAll,
                    text = "Check all",
                    imageVector = Icons.Default.DoneAll,
                    expandFrom = Alignment.Start,
                    shrinkTowards = Alignment.Start,
                    onClick = onCheckAllClick,
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                CurrentListBulkActionButton(
                    visible = canUncheckAll,
                    text = "Uncheck all",
                    imageVector = Icons.Default.RemoveDone,
                    expandFrom = Alignment.End,
                    shrinkTowards = Alignment.End,
                    onClick = onUncheckAllClick,
                )
            }
        }
    }
}

@Composable
private fun CurrentListBulkActionButton(
    visible: Boolean,
    text: String,
    imageVector: ImageVector,
    expandFrom: Alignment.Horizontal,
    shrinkTowards: Alignment.Horizontal,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandHorizontally(expandFrom = expandFrom),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = shrinkTowards),
    ) {
        Button(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun CurrentListOverflowMenu(
    expanded: Boolean,
    showAddTestItems: Boolean,
    canAddTestItems: Boolean,
    canShare: Boolean,
    canClear: Boolean,
    onIntent: (CurrentListOverflowMenuIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { onIntent(CurrentListOverflowMenuIntent.Dismiss) },
    ) {
        if (showAddTestItems) {
            DropdownMenuItem(
                enabled = canAddTestItems,
                text = { Text(text = "Add test items") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                onClick = { onIntent(CurrentListOverflowMenuIntent.AddTestItemsClick) },
            )
        }
        DropdownMenuItem(
            enabled = canShare,
            text = { Text(text = "Share list") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                )
            },
            onClick = { onIntent(CurrentListOverflowMenuIntent.ShareClick) },
        )
        DropdownMenuItem(
            enabled = canClear,
            text = {
                Text(
                    text = "Clear list",
                    color = if (canClear) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = if (canClear) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            },
            onClick = { onIntent(CurrentListOverflowMenuIntent.ClearClick) },
        )
    }
}

private sealed interface CurrentListOverflowMenuIntent {
    data object Dismiss : CurrentListOverflowMenuIntent

    data object AddTestItemsClick : CurrentListOverflowMenuIntent

    data object ShareClick : CurrentListOverflowMenuIntent

    data object ClearClick : CurrentListOverflowMenuIntent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemoveCurrentListItem(
    item: CurrentListItemUiState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * SwipeDismissThresholdFraction },
    )
    val coroutineScope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                coroutineScope.launch {
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    onRemove()
                }
            }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove ${item.title}",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = {
            content()
        },
    )
}

private const val SwipeDismissThresholdFraction = 0.5f

@Composable
private fun CurrentListItemCard(
    item: CurrentListItemUiState,
    isDragHandleActive: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = dragHandleModifier
                    .size(36.dp)
                    .background(
                        color = if (isDragHandleActive) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Reorder item",
                    tint = if (isDragHandleActive) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyCurrentListContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            }
        }
        Text(
            text = "No list items yet",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Add anything you want to keep around until you clear it.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        RoutineDialogFilledButton(
            text = "Add item",
            onClick = onAddClick,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
private fun AddCurrentListItemDialog(
    onDismiss: () -> Unit,
    onAddClick: (title: String, description: String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        title = {
            Text(text = "Add item")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RoutineOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    isRequired = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                RoutineOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            RoutineDialogFilledButton(
                text = "Add",
                enabled = title.isNotBlank(),
                onClick = { onAddClick(title, description) },
            )
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun ClearCurrentListConfirmationDialog(
    onDismiss: () -> Unit,
    onClearClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Clear list?") },
        text = { Text(text = "This removes every item from your current list.") },
        confirmButton = {
            RoutineDialogTextButton(
                text = "Clear",
                onClick = onClearClick,
                isDestructive = true,
            )
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

private val CurrentListBottomSafeSpace = 128.dp
private val CurrentListFabBottomClearance = 78.dp

@Preview(showBackground = true, widthDp = 390, heightDp = 852)
@Composable
private fun CurrentListComponentPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        CurrentListComponent(
            uiState = CurrentListUiState.preview(),
            onIntent = {},
        )
    }
}
