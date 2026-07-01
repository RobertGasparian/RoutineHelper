package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.formatter.SnapshotShareTextFormatter
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val snapshotShareTextFormatter: SnapshotShareTextFormatter,
    snapshotSummariesUseCase: SnapshotSummariesUseCase,
    private val snapshotUseCase: SnapshotUseCase,
) : BaseViewModel<HistoryUiState, HistoryIntent, Nothing>() {
    private val selectedSnapshotIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectedFilter = MutableStateFlow(HistoryFilter.All)
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)

    override val uiState: StateFlow<HistoryUiState> =
        combine(
            snapshotSummariesUseCase(),
            selectedFilter,
            selectedSnapshotIds,
            isShareFormatDialogVisible,
            shareDraft,
        ) { summaries, selectedFilter, selectedIds, isShareFormatDialogVisible, shareDraft ->
            val filteredSummaries = summaries.filter { summary ->
                selectedFilter.snapshotCadence == null || summary.cadence == selectedFilter.snapshotCadence
            }
            val existingIds = filteredSummaries.map(RoutineSnapshotSummary::snapshotId).toSet()
            val effectiveSelectedIds = selectedIds.intersect(existingIds)
            HistoryUiState(
                snapshots = filteredSummaries.map { summary ->
                    summary.toHistorySnapshotUiState(isSelected = summary.snapshotId in effectiveSelectedIds)
                },
                selectedFilter = selectedFilter,
                isSelectionMode = effectiveSelectedIds.isNotEmpty(),
                selectedCount = effectiveSelectedIds.size,
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
            )
        }
            .stateInViewModel(initialValue = HistoryUiState())

    override fun handleIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.ShareFileConfirm,
            is HistoryIntent.ShareTextConfirm,
            is HistoryIntent.SnapshotClick -> Unit
            HistoryIntent.ClearSelectionClick -> clearSelection()
            HistoryIntent.DeleteSelectedClick -> deleteSelectedSnapshots()
            is HistoryIntent.FilterClick -> selectFilter(intent.filter)
            HistoryIntent.ShareAsFileClick -> showFileSharePreview()
            HistoryIntent.ShareAsTextClick -> showTextSharePreview()
            HistoryIntent.ShareDismiss -> dismissSharePreview()
            is HistoryIntent.ShareFileNameChange -> updateShareFileName(intent.fileName)
            HistoryIntent.ShareSelectedClick -> showShareOptions()
            is HistoryIntent.ShareTextChange -> updateShareText(intent.text)
            is HistoryIntent.ToggleSnapshotSelection -> toggleSelection(intent.snapshotId)
        }
    }

    private fun toggleSelection(snapshotId: Long) {
        selectedSnapshotIds.value = selectedSnapshotIds.value.toggle(snapshotId)
    }

    private fun clearSelection() {
        selectedSnapshotIds.value = emptySet()
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    private fun selectFilter(filter: HistoryFilter) {
        selectedFilter.value = filter
        selectedSnapshotIds.value = emptySet()
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    private fun showShareOptions() {
        if (selectedSnapshotIds.value.isNotEmpty()) {
            isShareFormatDialogVisible.value = true
        }
    }

    private fun showTextSharePreview() {
        showSharePreview(mode = ShareMode.Text)
    }

    private fun showFileSharePreview() {
        showSharePreview(mode = ShareMode.File)
    }

    private fun showSharePreview(mode: ShareMode) {
        val snapshotIds = selectedSnapshotIds.value.toList()
        if (snapshotIds.isEmpty()) return

        isShareFormatDialogVisible.value = false
        launch {
            val snapshots = snapshotIds.mapNotNull { snapshotId ->
                snapshotUseCase(snapshotId).first()
            }
            if (snapshots.isNotEmpty()) {
                val exportText = snapshotShareTextFormatter(snapshots)
                shareDraft.value = when (mode) {
                    ShareMode.Text -> ShareDraft.text(exportText)
                    ShareMode.File -> ShareDraft.file(
                        messageText = snapshots.toHistoryFileShareMessage(),
                        fileText = exportText,
                        fileName = "routine-snapshots-export.txt",
                    )
                }
            }
        }
    }

    private fun updateShareFileName(fileName: String) {
        shareDraft.value = shareDraft.value?.copy(fileName = fileName)
    }

    private fun updateShareText(text: String) {
        shareDraft.value = shareDraft.value?.copy(messageText = text)
    }

    private fun dismissSharePreview() {
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    private fun deleteSelectedSnapshots() {
        val snapshotIds = selectedSnapshotIds.value.toList()
        if (snapshotIds.isEmpty()) return

        launch {
            snapshotIds.forEach { snapshotId ->
                deleteSnapshotUseCase(snapshotId)
            }
            clearSelection()
        }
    }
}

private enum class ShareMode {
    Text,
    File,
}

private fun Set<Long>.toggle(snapshotId: Long): Set<Long> =
    if (snapshotId in this) {
        this - snapshotId
    } else {
        this + snapshotId
    }
