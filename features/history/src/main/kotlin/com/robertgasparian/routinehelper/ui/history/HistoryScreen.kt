package com.robertgasparian.routinehelper.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.history.R
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import com.robertgasparian.routinehelper.ui.share.ShareFileDialog
import com.robertgasparian.routinehelper.ui.share.ShareFormatDialog
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.share.shareTextFile
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun HistoryScreen(
    onSnapshotClick: (snapshotId: Long) -> Unit,
    onShareTextPreviewClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareTitle = stringResource(R.string.history_share_snapshots_chooser)

    LaunchedEffect(uiState.shareDraft) {
        when (val draft = uiState.shareDraft) {
            is ShareDraft.Text -> {
                viewModel.onIntent(HistoryIntent.ShareDismiss)
                onShareTextPreviewClick(draft.messageText)
            }
            is ShareDraft.File,
            null -> Unit
        }
    }

    HistoryComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                HistoryIntent.SettingsClick -> onSettingsClick()
                is HistoryIntent.ShareFileConfirm -> {
                    context.shareTextFile(
                        fileText = intent.draft.fileText,
                        messageText = intent.draft.messageText,
                        title = shareTitle,
                        fileName = intent.draft.fileName,
                    )
                    viewModel.onIntent(HistoryIntent.ShareDismiss)
                }
                is HistoryIntent.ShareTextConfirm -> {
                    context.shareText(text = intent.messageText, title = shareTitle)
                    viewModel.onIntent(HistoryIntent.ShareDismiss)
                }
                is HistoryIntent.SnapshotClick -> {
                    if (uiState.isSelectionMode) {
                        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(intent.snapshotId))
                    } else {
                        onSnapshotClick(intent.snapshotId)
                    }
                }
                else -> viewModel.onIntent(intent)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryComponent(
    uiState: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { onIntent(HistoryIntent.ClearSelectionClick) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.history_clear_selection),
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isSelectionMode) {
                            pluralStringResource(
                                R.plurals.history_selected_count,
                                uiState.selectedCount,
                                uiState.selectedCount,
                            )
                        } else {
                            stringResource(R.string.history_title)
                        },
                    )
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(
                            enabled = uiState.selectedCount > 0,
                            onClick = { onIntent(HistoryIntent.ShareSelectedClick) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.history_share_selected_snapshots),
                            )
                        }
                        IconButton(
                            enabled = uiState.selectedCount > 0,
                            onClick = { onIntent(HistoryIntent.DeleteSelectedClick) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.history_delete_selected_snapshots),
                            )
                        }
                    } else {
                        IconButton(onClick = { onIntent(HistoryIntent.SettingsClick) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.history_open_settings),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HistoryFilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterClick = { filter -> onIntent(HistoryIntent.FilterClick(filter)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (uiState.snapshots.isEmpty()) {
                EmptyHistoryContent(
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 4.dp,
                        end = 16.dp,
                        bottom = 112.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.snapshots,
                        key = { snapshot -> snapshot.snapshotId },
                    ) { snapshot ->
                        val title = if (snapshot.cadence == RoutineCadence.Weekly) {
                            stringResource(R.string.history_week_of, snapshot.date)
                        } else {
                            snapshot.date
                        }
                        val completionLabel = when {
                            snapshot.totalCount == 0 -> stringResource(R.string.history_no_actions_saved)
                            snapshot.isComplete -> stringResource(R.string.history_all_completed)
                            else -> stringResource(
                                R.string.history_completion_fraction,
                                snapshot.completedCount,
                                snapshot.totalCount,
                            )
                        }
                        RoutineHistoryItemCard(
                            cadence = snapshot.cadence,
                            title = title,
                            completionLabel = completionLabel,
                            isComplete = snapshot.isComplete,
                            hasSummaryNote = snapshot.hasSummaryNote,
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = snapshot.isSelected,
                            onClick = { onIntent(HistoryIntent.SnapshotClick(snapshot.snapshotId)) },
                            onLongClick = { onIntent(HistoryIntent.ToggleSnapshotSelection(snapshot.snapshotId)) },
                        )
                    }
                }
            }
        }
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = { onIntent(HistoryIntent.ShareDismiss) },
            onTextClick = { onIntent(HistoryIntent.ShareAsTextClick) },
            onFileClick = { onIntent(HistoryIntent.ShareAsFileClick) },
        )
    }

    (uiState.shareDraft as? ShareDraft.File)?.let { draft ->
        ShareFileDialog(
            draft = draft,
            onFileNameChange = { fileName -> onIntent(HistoryIntent.ShareFileNameChange(fileName)) },
            onTextChange = { text -> onIntent(HistoryIntent.ShareTextChange(text)) },
            onDismiss = { onIntent(HistoryIntent.ShareDismiss) },
            onShareClick = { onIntent(HistoryIntent.ShareFileConfirm(draft)) },
        )
    }
}

@Composable
private fun HistoryFilterRow(
    selectedFilter: HistoryFilter,
    onFilterClick: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HistoryFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterClick(filter) },
                label = { Text(text = stringResource(filter.labelRes)) },
                leadingIcon = filter.snapshotCadence?.let { cadence ->
                    {
                        Icon(
                            imageVector = cadence.historyIcon,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
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
            text = stringResource(R.string.history_no_snapshots_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.history_no_snapshots_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryComponentPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onIntent = {},
        )
    }
}
