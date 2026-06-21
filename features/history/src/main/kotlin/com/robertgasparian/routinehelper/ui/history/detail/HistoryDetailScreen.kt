package com.robertgasparian.routinehelper.ui.history.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.history.BuildConfig
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.dsm.SummaryNoteCard
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import com.robertgasparian.routinehelper.ui.share.ShareFileDialog
import com.robertgasparian.routinehelper.ui.share.ShareFormatDialog
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.share.shareTextFile
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface HistoryDetailUiEvent {
    data object BackClick : HistoryDetailUiEvent

    data object ShareClick : HistoryDetailUiEvent

    data object ShareAsTextClick : HistoryDetailUiEvent

    data object ShareAsFileClick : HistoryDetailUiEvent

    data class ShareTextChange(
        val text: String,
    ) : HistoryDetailUiEvent

    data class ShareFileNameChange(
        val fileName: String,
    ) : HistoryDetailUiEvent

    data object ShareDismiss : HistoryDetailUiEvent

    data class ShareTextConfirm(
        val messageText: String,
    ) : HistoryDetailUiEvent

    data class ShareFileConfirm(
        val draft: ShareDraft,
    ) : HistoryDetailUiEvent

    data object DeleteClick : HistoryDetailUiEvent
}

@Composable
fun HistoryDetailScreen(
    snapshotId: Long,
    onBackClick: () -> Unit,
    onShareTextPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryDetailViewModel = hiltViewModel<HistoryDetailViewModel, HistoryDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(snapshotId) },
    ),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = HistoryDetailUiState(),
    )

    LaunchedEffect(uiState.shareDraft) {
        val draft = uiState.shareDraft
        if (draft != null && !draft.isFileShare) {
            viewModel.dismissSharePreview()
            onShareTextPreviewClick(draft.messageText)
        }
    }

    HistoryDetailComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HistoryDetailUiEvent.BackClick -> onBackClick()
                HistoryDetailUiEvent.DeleteClick -> viewModel.deleteSnapshot(onDeleted = onBackClick)
                HistoryDetailUiEvent.ShareAsFileClick -> viewModel.showFileSharePreview()
                HistoryDetailUiEvent.ShareAsTextClick -> viewModel.showTextSharePreview()
                HistoryDetailUiEvent.ShareClick -> viewModel.showShareOptions()
                HistoryDetailUiEvent.ShareDismiss -> viewModel.dismissSharePreview()
                is HistoryDetailUiEvent.ShareFileConfirm -> {
                    context.shareTextFile(
                        fileText = event.draft.fileText.orEmpty(),
                        messageText = event.draft.messageText,
                        title = "Share routine snapshot",
                        fileName = event.draft.fileName.orEmpty(),
                    )
                    viewModel.dismissSharePreview()
                }
                is HistoryDetailUiEvent.ShareFileNameChange -> viewModel.updateShareFileName(event.fileName)
                is HistoryDetailUiEvent.ShareTextChange -> viewModel.updateShareText(event.text)
                is HistoryDetailUiEvent.ShareTextConfirm -> {
                    context.shareText(text = event.messageText, title = "Share routine snapshot")
                    viewModel.dismissSharePreview()
                }
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailComponent(
    uiState: HistoryDetailUiState,
    onEvent: (HistoryDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    showDeleteAction: Boolean = BuildConfig.DEBUG,
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    var hiddenActionsExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(HistoryDetailUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(text = "Snapshot")
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isMissing && uiState.date.isNotBlank(),
                        onClick = { onEvent(HistoryDetailUiEvent.ShareClick) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share snapshot",
                        )
                    }
                    if (showDeleteAction) {
                        IconButton(
                            onClick = {
                                // TODO Remove this test-only delete affordance when history management UX is finalized.
                                showDeleteConfirmation = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete snapshot",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isMissing -> MissingSnapshotContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            uiState.items.isEmpty() -> EmptySnapshotContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 112.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    HistoryDetailHeaderCard(uiState = uiState)
                }
                if (uiState.summaryNote.isNotBlank()) {
                    item {
                        SummaryNoteCard(
                            note = uiState.summaryNote,
                            label = if (uiState.cadence == RoutineCadence.Weekly) {
                                "Week note"
                            } else {
                                "Day note"
                            },
                            onEditClick = {},
                            isEditable = false,
                        )
                    }
                }
                items(
                    items = uiState.visibleItems,
                    key = { item -> item.actionId },
                ) { item ->
                    HistoryDetailActionItemCard(item = item)
                }
                if (uiState.hiddenItems.isNotEmpty()) {
                    item {
                        HiddenActionsHeader(
                            count = uiState.hiddenItems.size,
                            isExpanded = hiddenActionsExpanded,
                            onClick = { hiddenActionsExpanded = !hiddenActionsExpanded },
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = hiddenActionsExpanded,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 260),
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 180, delayMillis = 60),
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 220),
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 120),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                uiState.hiddenItems.forEach { item ->
                                    HistoryDetailActionItemCard(item = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "Delete snapshot?") },
            text = { Text(text = "This removes this saved history entry from the test database.") },
            confirmButton = {
                RoutineDialogTextButton(
                    text = "Delete",
                    onClick = {
                        showDeleteConfirmation = false
                        onEvent(HistoryDetailUiEvent.DeleteClick)
                    },
                    isDestructive = true,
                )
            },
            dismissButton = {
                RoutineDialogTextButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirmation = false },
                )
            },
        )
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = { onEvent(HistoryDetailUiEvent.ShareDismiss) },
            onTextClick = { onEvent(HistoryDetailUiEvent.ShareAsTextClick) },
            onFileClick = { onEvent(HistoryDetailUiEvent.ShareAsFileClick) },
        )
    }

    uiState.shareDraft?.takeIf { draft -> draft.isFileShare }?.let { draft ->
        ShareSnapshotDialog(
            draft = draft,
            onFileNameChange = { fileName -> onEvent(HistoryDetailUiEvent.ShareFileNameChange(fileName)) },
            onTextChange = { text -> onEvent(HistoryDetailUiEvent.ShareTextChange(text)) },
            onDismiss = { onEvent(HistoryDetailUiEvent.ShareDismiss) },
            onShareClick = { onEvent(HistoryDetailUiEvent.ShareFileConfirm(draft)) },
        )
    }
}

@Composable
private fun HiddenActionsHeader(
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "HiddenActionsArrowRotation",
    )
    val title = "$count Hidden ${if (count == 1) "Action" else "Actions"}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.rotate(arrowRotation),
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse hidden actions" else "Expand hidden actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShareSnapshotDialog(
    draft: ShareDraft,
    onFileNameChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
) {
    ShareFileDialog(
        draft = draft,
        onFileNameChange = onFileNameChange,
        onTextChange = onTextChange,
        onDismiss = onDismiss,
        onShareClick = onShareClick,
    )
}

@Composable
private fun MissingSnapshotContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Snapshot not found",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun EmptySnapshotContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No items in this snapshot",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryDetailComponentPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onEvent = {},
        )
    }
}
