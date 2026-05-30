package com.robertgasparian.routinehelper.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import com.robertgasparian.routinehelper.ui.share.ShareFormatDialog
import com.robertgasparian.routinehelper.ui.share.ShareTextDialog
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.share.shareTextFile
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface HistoryUiEvent {
    data class SnapshotClick(
        val snapshotId: Long,
    ) : HistoryUiEvent

    data class SnapshotLongClick(
        val snapshotId: Long,
    ) : HistoryUiEvent

    data object ClearSelectionClick : HistoryUiEvent

    data object ShareSelectedClick : HistoryUiEvent

    data object ShareAsTextClick : HistoryUiEvent

    data object ShareAsFileClick : HistoryUiEvent

    data object DeleteSelectedClick : HistoryUiEvent

    data class ShareTextChange(
        val text: String,
    ) : HistoryUiEvent

    data object ShareDismiss : HistoryUiEvent

    data class ShareTextConfirm(
        val messageText: String,
    ) : HistoryUiEvent

    data class ShareFileConfirm(
        val draft: ShareDraft,
    ) : HistoryUiEvent
}

@Composable
fun HistoryScreen(
    onSnapshotClick: (snapshotId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HistoryUiEvent.ClearSelectionClick -> viewModel.clearSelection()
                HistoryUiEvent.DeleteSelectedClick -> viewModel.deleteSelectedSnapshots()
                HistoryUiEvent.ShareAsFileClick -> viewModel.showFileSharePreview()
                HistoryUiEvent.ShareAsTextClick -> viewModel.showTextSharePreview()
                HistoryUiEvent.ShareDismiss -> viewModel.dismissSharePreview()
                is HistoryUiEvent.ShareFileConfirm -> {
                    context.shareTextFile(
                        fileText = event.draft.fileText.orEmpty(),
                        messageText = event.draft.messageText,
                        title = "Share routine snapshots",
                        fileName = "routine-snapshots-export.txt",
                    )
                    viewModel.dismissSharePreview()
                }
                HistoryUiEvent.ShareSelectedClick -> viewModel.showShareOptions()
                is HistoryUiEvent.ShareTextChange -> viewModel.updateShareText(event.text)
                is HistoryUiEvent.ShareTextConfirm -> {
                    context.shareText(text = event.messageText, title = "Share routine snapshots")
                    viewModel.dismissSharePreview()
                }
                is HistoryUiEvent.SnapshotClick -> {
                    if (uiState.isSelectionMode) {
                        viewModel.toggleSelection(event.snapshotId)
                    } else {
                        onSnapshotClick(event.snapshotId)
                    }
                }
                is HistoryUiEvent.SnapshotLongClick -> viewModel.toggleSelection(event.snapshotId)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryComponent(
    uiState: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { onEvent(HistoryUiEvent.ClearSelectionClick) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection",
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isSelectionMode) {
                            "${uiState.selectedCount} selected"
                        } else {
                            "History"
                        },
                    )
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(
                            enabled = uiState.selectedCount > 0,
                            onClick = { onEvent(HistoryUiEvent.ShareSelectedClick) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share selected snapshots",
                            )
                        }
                        IconButton(
                            enabled = uiState.selectedCount > 0,
                            onClick = { onEvent(HistoryUiEvent.DeleteSelectedClick) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected snapshots",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.snapshots.isEmpty()) {
            EmptyHistoryContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
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
                items(
                    items = uiState.snapshots,
                    key = { snapshot -> snapshot.snapshotId },
                ) { snapshot ->
                    HistorySnapshotCard(
                        snapshot = snapshot,
                        isSelectionMode = uiState.isSelectionMode,
                        onClick = { onEvent(HistoryUiEvent.SnapshotClick(snapshot.snapshotId)) },
                        onLongClick = { onEvent(HistoryUiEvent.SnapshotLongClick(snapshot.snapshotId)) },
                    )
                }
            }
        }
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = { onEvent(HistoryUiEvent.ShareDismiss) },
            onTextClick = { onEvent(HistoryUiEvent.ShareAsTextClick) },
            onFileClick = { onEvent(HistoryUiEvent.ShareAsFileClick) },
        )
    }

    uiState.shareDraft?.let { draft ->
        ShareTextDialog(
            draft = draft,
            onTextChange = { text -> onEvent(HistoryUiEvent.ShareTextChange(text)) },
            onDismiss = { onEvent(HistoryUiEvent.ShareDismiss) },
            onShareClick = {
                if (draft.isFileShare) {
                    onEvent(HistoryUiEvent.ShareFileConfirm(draft))
                } else {
                    onEvent(HistoryUiEvent.ShareTextConfirm(draft.messageText))
                }
            },
        )
    }
}

@Composable
private fun EmptyHistoryContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
        )
        Text(
            text = "No snapshots yet",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Use the Snapshot action on Today while we build the nightly reset.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistorySnapshotCard(
    snapshot: HistorySnapshotUiState,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (snapshot.isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        ListItem(
            leadingContent = {
                if (isSelectionMode) {
                    Checkbox(
                        checked = snapshot.isSelected,
                        onCheckedChange = null,
                    )
                }
            },
            headlineContent = {
                Text(text = snapshot.date)
            },
            supportingContent = {
                Text(text = snapshot.finalizedLabel)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryComponentPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onEvent = {},
        )
    }
}
