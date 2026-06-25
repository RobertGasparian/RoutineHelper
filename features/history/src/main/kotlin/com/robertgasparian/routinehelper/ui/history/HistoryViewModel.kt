package com.robertgasparian.routinehelper.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.formatter.SnapshotShareTextFormatter
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val snapshotShareTextFormatter: SnapshotShareTextFormatter,
    snapshotSummariesUseCase: SnapshotSummariesUseCase,
    private val snapshotUseCase: SnapshotUseCase,
) : ViewModel() {
    private val selectedSnapshotIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectedFilter = MutableStateFlow(HistoryFilter.All)
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)

    val uiState: StateFlow<HistoryUiState> =
        combine(
            snapshotSummariesUseCase(),
            selectedFilter,
            selectedSnapshotIds,
            isShareFormatDialogVisible,
            shareDraft,
        ) { summaries, selectedFilter, selectedIds, isShareFormatDialogVisible, shareDraft ->
            val filteredSummaries = summaries.filter { summary ->
                selectedFilter.cadence == null || summary.cadence == selectedFilter.cadence
            }
            val existingIds = filteredSummaries.map(RoutineSnapshotSummary::snapshotId).toSet()
            val effectiveSelectedIds = selectedIds.intersect(existingIds)
            HistoryUiState(
                snapshots = filteredSummaries.map { summary ->
                    summary.toUiState(isSelected = summary.snapshotId in effectiveSelectedIds)
                },
                selectedFilter = selectedFilter,
                isSelectionMode = effectiveSelectedIds.isNotEmpty(),
                selectedCount = effectiveSelectedIds.size,
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState(),
            )

    fun toggleSelection(snapshotId: Long) {
        selectedSnapshotIds.value = selectedSnapshotIds.value.toggle(snapshotId)
    }

    fun clearSelection() {
        selectedSnapshotIds.value = emptySet()
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    fun selectFilter(filter: HistoryFilter) {
        selectedFilter.value = filter
        selectedSnapshotIds.value = emptySet()
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    fun showShareOptions() {
        if (selectedSnapshotIds.value.isNotEmpty()) {
            isShareFormatDialogVisible.value = true
        }
    }

    fun showTextSharePreview() {
        showSharePreview(mode = ShareMode.Text)
    }

    fun showFileSharePreview() {
        showSharePreview(mode = ShareMode.File)
    }

    private fun showSharePreview(mode: ShareMode) {
        val snapshotIds = selectedSnapshotIds.value.toList()
        if (snapshotIds.isEmpty()) return

        isShareFormatDialogVisible.value = false
        viewModelScope.launch {
            val snapshots = snapshotIds.mapNotNull { snapshotId ->
                snapshotUseCase(snapshotId).first()
            }
            if (snapshots.isNotEmpty()) {
                val exportText = snapshotShareTextFormatter(snapshots)
                shareDraft.value = when (mode) {
                    ShareMode.Text -> ShareDraft.text(exportText)
                    ShareMode.File -> ShareDraft.file(
                        messageText = snapshots.toFileShareMessage(),
                        fileText = exportText,
                        fileName = "routine-snapshots-export.txt",
                    )
                }
            }
        }
    }

    fun updateShareFileName(fileName: String) {
        shareDraft.value = shareDraft.value?.copy(fileName = fileName)
    }

    fun updateShareText(text: String) {
        shareDraft.value = shareDraft.value?.copy(messageText = text)
    }

    fun dismissSharePreview() {
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    fun deleteSelectedSnapshots() {
        val snapshotIds = selectedSnapshotIds.value.toList()
        if (snapshotIds.isEmpty()) return

        viewModelScope.launch {
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

private fun List<RoutineSnapshot>.toFileShareMessage(): String {
    val dates = map { snapshot -> snapshot.date }.distinct().sorted()
    return when (dates.size) {
        0 -> "Here are the routine snapshots."
        1 -> "Here are the routine snapshots from ${dates.first()}."
        else -> "Here are the routine snapshots from ${dates.first()} to ${dates.last()}."
    }
}

private fun RoutineSnapshotSummary.toUiState(isSelected: Boolean): HistorySnapshotUiState =
    HistorySnapshotUiState(
        snapshotId = snapshotId,
        date = if (cadence == RoutineCadence.Weekly) "Week of $date" else date,
        cadence = cadence,
        completedCount = completedCount,
        totalCount = totalCount,
        hasSummaryNote = hasSummaryNote,
        isSelected = isSelected,
    )

private val HistoryFilter.cadence: RoutineCadence?
    get() = when (this) {
        HistoryFilter.All -> null
        HistoryFilter.Daily -> RoutineCadence.Daily
        HistoryFilter.Weekly -> RoutineCadence.Weekly
    }

private fun Set<Long>.toggle(snapshotId: Long): Set<Long> =
    if (snapshotId in this) {
        this - snapshotId
    } else {
        this + snapshotId
    }
