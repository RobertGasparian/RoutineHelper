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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.history.BuildConfig
import com.robertgasparian.routinehelper.features.history.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionCard
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSession
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import com.robertgasparian.routinehelper.ui.share.ShareFileDialog
import com.robertgasparian.routinehelper.ui.share.ShareFormatDialog
import com.robertgasparian.routinehelper.ui.share.shareText
import com.robertgasparian.routinehelper.ui.share.shareTextFile
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun HistoryDetailScreen(
    snapshotId: Long,
    initialAction: HistoryDetailInitialAction? = null,
    onBackClick: () -> Unit,
    onSummaryEditorClick: () -> Unit,
    onInitialSummaryEditorUnavailable: () -> Unit,
    reflectionEditorSession: ReflectionEditorSession,
    onShareTextPreviewClick: (String) -> Unit,
    onDebugSummaryNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HistoryDetailViewModel = hiltViewModel<HistoryDetailViewModel, HistoryDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(snapshotId) },
    ),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reflectionState by reflectionEditorSession.state.collectAsStateWithLifecycle()
    val shareTitle = stringResource(R.string.history_share_snapshot_chooser)
    var initialActionConsumed by rememberSaveable(snapshotId, initialAction) {
        mutableStateOf(false)
    }
    LaunchedEffect(
        initialAction,
        initialActionConsumed,
        uiState.isLoading,
        uiState.isMissing,
        uiState.isReflectionEditable,
    ) {
        if (
            initialActionConsumed ||
            initialAction == null ||
            uiState.isLoading
        ) {
            return@LaunchedEffect
        }

        initialActionConsumed = true
        if (
            initialAction == HistoryDetailInitialAction.OpenSummaryEditor &&
            !uiState.isMissing &&
            uiState.isReflectionEditable
        ) {
            reflectionEditorSession.start(
                ReflectionEditorInitialState(
                    text = uiState.summaryNote,
                    rating = uiState.rating,
                ),
            )
        } else {
            onInitialSummaryEditorUnavailable()
        }
    }

    LaunchedEffect(
        reflectionState.saveRequest?.requestId,
        uiState.isLoading,
        uiState.isMissing,
        uiState.isReflectionEditable,
    ) {
        val request = reflectionState.saveRequest ?: return@LaunchedEffect
        if (uiState.isLoading) return@LaunchedEffect

        if (!uiState.isMissing && uiState.isReflectionEditable) {
            viewModel.onIntent(
                HistoryDetailIntent.SaveReflection(
                    summaryNote = request.text,
                    rating = request.rating,
                ),
            )
        }
        reflectionEditorSession.consumeSaveRequest(request.requestId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                HistoryDetailUiEvent.SnapshotDeleted -> onBackClick()
            }
        }
    }

    LaunchedEffect(uiState.shareDraft) {
        when (val draft = uiState.shareDraft) {
            is ShareDraft.Text -> {
                viewModel.onIntent(HistoryDetailIntent.ShareDismiss)
                onShareTextPreviewClick(draft.messageText)
            }
            is ShareDraft.File,
            null -> Unit
        }
    }

    HistoryDetailComponent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                HistoryDetailIntent.BackClick -> onBackClick()
                HistoryDetailIntent.DebugSummaryNotificationClick -> onDebugSummaryNotificationClick()
                HistoryDetailIntent.EditReflectionClick -> {
                    if (uiState.isReflectionEditable && !uiState.isMissing) {
                        reflectionEditorSession.start(
                            ReflectionEditorInitialState(
                                text = uiState.summaryNote,
                                rating = uiState.rating,
                            ),
                        )
                        onSummaryEditorClick()
                    }
                }
                is HistoryDetailIntent.ShareFileConfirm -> {
                    context.shareTextFile(
                        fileText = intent.draft.fileText,
                        messageText = intent.draft.messageText,
                        title = shareTitle,
                        fileName = intent.draft.fileName,
                    )
                    viewModel.onIntent(HistoryDetailIntent.ShareDismiss)
                }
                is HistoryDetailIntent.ShareTextConfirm -> {
                    context.shareText(text = intent.messageText, title = shareTitle)
                    viewModel.onIntent(HistoryDetailIntent.ShareDismiss)
                }
                else -> viewModel.onIntent(intent)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailComponent(
    uiState: HistoryDetailUiState,
    onIntent: (HistoryDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    showDebugNotificationAction: Boolean = BuildConfig.DEBUG,
    showDeleteAction: Boolean = BuildConfig.DEBUG,
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    var hiddenActionsExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onIntent(HistoryDetailIntent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.history_back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.history_snapshot_title))
                },
                actions = {
                    if (showDebugNotificationAction) {
                        IconButton(
                            enabled = !uiState.isMissing,
                            onClick = {
                                onIntent(HistoryDetailIntent.DebugSummaryNotificationClick)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(
                                    R.string.history_send_test_summary_notification,
                                ),
                            )
                        }
                    }
                    IconButton(
                        enabled = !uiState.isMissing && uiState.date.isNotBlank(),
                        onClick = { onIntent(HistoryDetailIntent.ShareClick) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.history_share_snapshot),
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
                                contentDescription = stringResource(R.string.history_delete_snapshot),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingSnapshotContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

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
                if (!uiState.hasReflection) {
                    if (uiState.isReflectionEditable) {
                        item {
                            Button(
                                onClick = { onIntent(HistoryDetailIntent.EditReflectionClick) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.history_add_summary))
                            }
                        }
                    }
                } else {
                    item {
                        ReflectionCard(
                            summaryNote = uiState.summaryNote,
                            rating = uiState.rating,
                            label = if (uiState.cadence == RoutineCadence.Weekly) {
                                stringResource(R.string.history_week_note)
                            } else {
                                stringResource(R.string.history_day_note)
                            },
                            onEditClick = { onIntent(HistoryDetailIntent.EditReflectionClick) },
                            isEditable = uiState.isReflectionEditable,
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
            title = { Text(text = stringResource(R.string.history_delete_snapshot_title)) },
            text = { Text(text = stringResource(R.string.history_delete_snapshot_message)) },
            confirmButton = {
                RoutineDialogTextButton(
                    text = stringResource(R.string.history_delete),
                    onClick = {
                        showDeleteConfirmation = false
                        onIntent(HistoryDetailIntent.DeleteClick)
                    },
                    isDestructive = true,
                )
            },
            dismissButton = {
                RoutineDialogTextButton(
                    text = stringResource(R.string.history_cancel),
                    onClick = { showDeleteConfirmation = false },
                )
            },
        )
    }

    if (uiState.isShareFormatDialogVisible) {
        ShareFormatDialog(
            onDismiss = { onIntent(HistoryDetailIntent.ShareDismiss) },
            onTextClick = { onIntent(HistoryDetailIntent.ShareAsTextClick) },
            onFileClick = { onIntent(HistoryDetailIntent.ShareAsFileClick) },
        )
    }

    (uiState.shareDraft as? ShareDraft.File)?.let { draft ->
        ShareSnapshotDialog(
            draft = draft,
            onFileNameChange = { fileName -> onIntent(HistoryDetailIntent.ShareFileNameChange(fileName)) },
            onTextChange = { text -> onIntent(HistoryDetailIntent.ShareTextChange(text)) },
            onDismiss = { onIntent(HistoryDetailIntent.ShareDismiss) },
            onShareClick = { onIntent(HistoryDetailIntent.ShareFileConfirm(draft)) },
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
    val title = pluralStringResource(R.plurals.history_hidden_actions, count, count)

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
                contentDescription = stringResource(
                    if (isExpanded) {
                        R.string.history_collapse_hidden_actions
                    } else {
                        R.string.history_expand_hidden_actions
                    },
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShareSnapshotDialog(
    draft: ShareDraft.File,
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
private fun LoadingSnapshotContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
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
            text = stringResource(R.string.history_snapshot_not_found),
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
            text = stringResource(R.string.history_snapshot_empty),
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
            onIntent = {},
        )
    }
}
