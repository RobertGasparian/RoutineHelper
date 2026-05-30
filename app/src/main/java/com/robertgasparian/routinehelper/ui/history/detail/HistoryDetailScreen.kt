package com.robertgasparian.routinehelper.ui.history.detail

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
fun HistoryDetailScreen(
    snapshotId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryDetailViewModel = hiltViewModel<HistoryDetailViewModel, HistoryDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(snapshotId) },
    ),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = HistoryDetailUiState(),
    )

    HistoryDetailComponent(
        uiState = uiState,
        onBackClick = onBackClick,
        onShareClick = viewModel::showShareOptions,
        onShareAsTextClick = viewModel::showTextSharePreview,
        onShareAsFileClick = viewModel::showFileSharePreview,
        onShareTextChange = viewModel::updateShareText,
        onShareDismiss = viewModel::dismissSharePreview,
        onShareTextConfirm = { messageText ->
            context.shareText(text = messageText, title = "Share routine snapshot")
            viewModel.dismissSharePreview()
        },
        onShareFileConfirm = { draft ->
            context.shareTextFile(
                fileText = draft.fileText.orEmpty(),
                messageText = draft.messageText,
                title = "Share routine snapshot",
                fileName = "routine-snapshot-${uiState.date.ifBlank { "export" }}.txt",
            )
            viewModel.dismissSharePreview()
        },
        onDeleteClick = {
            viewModel.deleteSnapshot(
                onDeleted = onBackClick,
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailComponent(
    uiState: HistoryDetailUiState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onShareAsTextClick: () -> Unit,
    onShareAsFileClick: () -> Unit,
    onShareTextChange: (String) -> Unit,
    onShareDismiss: () -> Unit,
    onShareTextConfirm: (String) -> Unit,
    onShareFileConfirm: (ShareDraft) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Column {
                        Text(text = if (uiState.date.isBlank()) "Snapshot" else uiState.date)
                        if (uiState.finalizedLabel.isNotBlank()) {
                            Text(
                                text = uiState.finalizedLabel,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isMissing && uiState.date.isNotBlank(),
                        onClick = onShareClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share snapshot",
                        )
                    }
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
                items(
                    items = uiState.items,
                    key = { item -> item.actionId },
                ) { item ->
                    HistoryDetailItemCard(item = item)
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
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick()
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = onShareDismiss,
            onTextClick = onShareAsTextClick,
            onFileClick = onShareAsFileClick,
        )
    }

    uiState.shareDraft?.let { draft ->
        ShareSnapshotDialog(
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
private fun ShareSnapshotDialog(
    draft: ShareDraft,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
) {
    ShareTextDialog(
        draft = draft,
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

@Composable
private fun HistoryDetailItemCard(
    item: HistoryDetailItemUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
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
                }

                CompletionChip(isChecked = item.isChecked)
            }

            DetailSection(
                label = "Action description",
                text = item.description,
                emptyText = "No description saved for this action.",
            )

            DetailSection(
                label = "Day note",
                text = item.note,
                emptyText = "No note was added for this day.",
            )
        }
    }
}

@Composable
private fun CompletionChip(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    ElevatedAssistChip(
        modifier = modifier,
        onClick = {},
        leadingIcon = {
            Icon(
                imageVector = if (isChecked) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.DateRange
                },
                contentDescription = null,
            )
        },
        label = {
            Text(text = if (isChecked) "Checked" else "Unchecked")
        },
    )
}

@Composable
private fun DetailSection(
    label: String,
    text: String?,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text?.takeIf(String::isNotBlank) ?: emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (text.isNullOrBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryDetailComponentPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onBackClick = {},
            onShareClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
            onDeleteClick = {},
        )
    }
}
