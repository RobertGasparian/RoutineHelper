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
        onSnapshotClick = { snapshotId ->
            if (uiState.isSelectionMode) {
                viewModel.toggleSelection(snapshotId)
            } else {
                onSnapshotClick(snapshotId)
            }
        },
        onSnapshotLongClick = viewModel::toggleSelection,
        onClearSelectionClick = viewModel::clearSelection,
        onShareSelectedClick = viewModel::showShareOptions,
        onShareAsTextClick = viewModel::showTextSharePreview,
        onShareAsFileClick = viewModel::showFileSharePreview,
        onDeleteSelectedClick = viewModel::deleteSelectedSnapshots,
        onShareTextChange = viewModel::updateShareText,
        onShareDismiss = viewModel::dismissSharePreview,
        onShareTextConfirm = { messageText ->
            context.shareText(text = messageText, title = "Share routine snapshots")
            viewModel.dismissSharePreview()
        },
        onShareFileConfirm = { draft ->
            context.shareTextFile(
                fileText = draft.fileText.orEmpty(),
                messageText = draft.messageText,
                title = "Share routine snapshots",
                fileName = "routine-snapshots-export.txt",
            )
            viewModel.dismissSharePreview()
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryComponent(
    uiState: HistoryUiState,
    onSnapshotClick: (snapshotId: Long) -> Unit,
    onSnapshotLongClick: (snapshotId: Long) -> Unit,
    onClearSelectionClick: () -> Unit,
    onShareSelectedClick: () -> Unit,
    onShareAsTextClick: () -> Unit,
    onShareAsFileClick: () -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onShareTextChange: (String) -> Unit,
    onShareDismiss: () -> Unit,
    onShareTextConfirm: (String) -> Unit,
    onShareFileConfirm: (ShareDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = onClearSelectionClick) {
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
                            onClick = onShareSelectedClick,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share selected snapshots",
                            )
                        }
                        IconButton(
                            enabled = uiState.selectedCount > 0,
                            onClick = onDeleteSelectedClick,
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
                        onClick = { onSnapshotClick(snapshot.snapshotId) },
                        onLongClick = { onSnapshotLongClick(snapshot.snapshotId) },
                    )
                }
            }
        }
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = onShareDismiss,
            onTextClick = onShareAsTextClick,
            onFileClick = onShareAsFileClick,
        )
    }

    uiState.shareDraft?.let { draft ->
        ShareTextDialog(
            draft = draft,
            onTextChange = onShareTextChange,
            onDismiss = onShareDismiss,
            onShareClick = {
                if (draft.isFileShare) {
                    onShareFileConfirm(draft)
                } else {
                    onShareTextConfirm(draft.messageText)
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
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}
