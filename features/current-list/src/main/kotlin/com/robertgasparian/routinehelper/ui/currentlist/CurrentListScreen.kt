package com.robertgasparian.routinehelper.ui.currentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.features.currentlist.BuildConfig
import com.robertgasparian.routinehelper.features.currentlist.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogFilledButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.dsm.RoutineReorderDragStartMode
import com.robertgasparian.routinehelper.ui.dsm.RoutineReorderableLazyColumn
import com.robertgasparian.routinehelper.ui.dsm.RoutineSwipeToReveal
import com.robertgasparian.routinehelper.ui.dsm.rememberLazyListIsActuallyScrolling
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import kotlinx.coroutines.flow.filter

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
    var expandedItemMenuId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var revealedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()

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
        label = "CurrentListFabScale",
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.current_list_title)) },
                actions = {
                    IconButton(onClick = { onIntent(CurrentListIntent.SettingsClick) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.current_list_open_settings),
                        )
                    }
                    Box {
                        IconButton(onClick = { isOverflowMenuVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.current_list_more_options),
                            )
                        }
                        CurrentListOverflowMenu(
                            expanded = isOverflowMenuVisible,
                            showAddTestItems = showAddTestItems,
                            canShare = uiState.canShare,
                            canClear = uiState.hasItems && uiState.canRemoveItems,
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
                modifier = Modifier
                    .padding(bottom = CurrentListFabBottomClearance)
                    .scale(fabScale),
                onClick = {
                    if (fabVisible) {
                        isAddDialogVisible = true
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.current_list_add_item),
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
                dragStartMode = RoutineReorderDragStartMode.LongPress,
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
                LaunchedEffect(isDragHandleActive) {
                    if (isDragHandleActive && revealedItemId == item.id) {
                        revealedItemId = null
                    }
                }
                RoutineSwipeToReveal(
                    isRevealed = revealedItemId == item.id && !isDragging,
                    enabled = uiState.canRemoveItems && !isDragging,
                    onRevealedChange = { isRevealed ->
                        if (isRevealed) {
                            expandedItemMenuId = null
                            revealedItemId = item.id
                        } else if (revealedItemId == item.id) {
                            revealedItemId = null
                        }
                    },
                    onAction = { onIntent(CurrentListIntent.RemoveItem(item.id)) },
                    backgroundContent = {
                        CurrentListDeleteBackground()
                    },
                    actionContent = { onDeleteClick ->
                        CurrentListDeleteAction(
                            itemTitle = item.title,
                            onClick = onDeleteClick,
                        )
                    },
                ) {
                    CurrentListItemCard(
                        item = item,
                        isDragging = isDragging,
                        isOverflowMenuExpanded = expandedItemMenuId == item.id && !isDragging,
                        onIntent = { itemIntent ->
                            when (itemIntent) {
                                is CurrentListItemIntent.CheckedChange -> {
                                    onIntent(
                                        CurrentListIntent.CheckedChange(
                                            itemId = item.id,
                                            isChecked = itemIntent.isChecked,
                                        ),
                                    )
                                }
                                CurrentListItemIntent.OverflowMenuClick -> {
                                    revealedItemId = null
                                    expandedItemMenuId = item.id
                                }
                                CurrentListItemIntent.OverflowMenuDismiss -> {
                                    if (expandedItemMenuId == item.id) {
                                        expandedItemMenuId = null
                                    }
                                }
                                CurrentListItemIntent.EditClick -> {
                                    expandedItemMenuId = null
                                    editingItemId = item.id
                                }
                            }
                        },
                        modifier = dragHandleModifier,
                    )
                }
            }
        }
    }

    if (isAddDialogVisible) {
        CurrentListItemEditorDialog(
            dialogTitle = stringResource(R.string.current_list_add_item),
            confirmButtonText = stringResource(R.string.current_list_add),
            onDismiss = { isAddDialogVisible = false },
            onConfirmClick = { title, description ->
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

    val editingItem = editingItemId?.let { itemId ->
        uiState.items.firstOrNull { item -> item.id == itemId }
    }
    if (editingItem != null) {
        CurrentListItemEditorDialog(
            dialogTitle = stringResource(R.string.current_list_edit_item),
            confirmButtonText = stringResource(R.string.current_list_save),
            initialTitle = editingItem.title,
            initialDescription = editingItem.description,
            onDismiss = { editingItemId = null },
            onConfirmClick = { title, description ->
                editingItemId = null
                onIntent(
                    CurrentListIntent.UpdateItem(
                        itemId = editingItem.id,
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
                    text = stringResource(R.string.current_list_check_all),
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
                    text = stringResource(R.string.current_list_uncheck_all),
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
                text = {
                    Text(
                        text = stringResource(R.string.current_list_add_test_items),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = { onIntent(CurrentListOverflowMenuIntent.AddTestItemsClick) },
            )
        }
        DropdownMenuItem(
            enabled = canShare,
            text = { Text(text = stringResource(R.string.current_list_share_list)) },
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
                    text = stringResource(R.string.current_list_clear_list),
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

@Composable
private fun CurrentListDeleteBackground(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {}
}

@Composable
private fun CurrentListDeleteAction(
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
                contentDescription = stringResource(R.string.current_list_remove_item, itemTitle),
            )
        }
    }
}

@Composable
private fun CurrentListItemCard(
    item: CurrentListItemUiState,
    isDragging: Boolean,
    isOverflowMenuExpanded: Boolean,
    onIntent: (CurrentListItemIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragActivationProgress by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "Current list item drag activation",
    )
    val containerColor = lerp(
        start = MaterialTheme.colorScheme.surfaceContainerLow,
        stop = MaterialTheme.colorScheme.surfaceContainerHigh,
        fraction = dragActivationProgress,
    )
    val borderColor = lerp(
        start = MaterialTheme.colorScheme.outlineVariant,
        stop = MaterialTheme.colorScheme.primary,
        fraction = dragActivationProgress,
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(borderColor),
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
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
                onCheckedChange = { isChecked ->
                    onIntent(CurrentListItemIntent.CheckedChange(isChecked))
                },
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
            Box {
                IconButton(
                    enabled = !isDragging,
                    onClick = { onIntent(CurrentListItemIntent.OverflowMenuClick) },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.current_list_item_more_options, item.title),
                    )
                }
                DropdownMenu(
                    expanded = isOverflowMenuExpanded,
                    onDismissRequest = { onIntent(CurrentListItemIntent.OverflowMenuDismiss) },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.current_list_edit)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        },
                        onClick = { onIntent(CurrentListItemIntent.EditClick) },
                    )
                }
            }
        }
    }
}

private sealed interface CurrentListItemIntent {
    data class CheckedChange(
        val isChecked: Boolean,
    ) : CurrentListItemIntent

    data object OverflowMenuClick : CurrentListItemIntent

    data object OverflowMenuDismiss : CurrentListItemIntent

    data object EditClick : CurrentListItemIntent
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
            text = stringResource(R.string.current_list_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.current_list_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        RoutineDialogFilledButton(
            text = stringResource(R.string.current_list_add_item),
            onClick = onAddClick,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
private fun CurrentListItemEditorDialog(
    dialogTitle: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirmClick: (title: String, description: String?) -> Unit,
    initialTitle: String = "",
    initialDescription: String? = null,
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RoutineOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.current_list_item_title_label),
                    isRequired = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                RoutineOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = stringResource(R.string.current_list_item_description_label),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            RoutineDialogFilledButton(
                text = confirmButtonText,
                enabled = title.isNotBlank(),
                onClick = { onConfirmClick(title, description) },
            )
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.current_list_cancel),
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
        title = { Text(text = stringResource(R.string.current_list_clear_confirmation_title)) },
        text = { Text(text = stringResource(R.string.current_list_clear_confirmation_message)) },
        confirmButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.current_list_clear),
                onClick = onClearClick,
                isDestructive = true,
            )
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.current_list_cancel),
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
