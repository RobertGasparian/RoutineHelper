package com.robertgasparian.routinehelper.ui.history.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.formatter.SnapshotShareTextFormatter
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.ui.history.historyLabel
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    @Assisted private val snapshotId: Long,
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val snapshotShareTextFormatter: SnapshotShareTextFormatter,
    snapshotUseCase: SnapshotUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)
    private var currentSnapshot: RoutineSnapshot? = null

    val uiState: Flow<HistoryDetailUiState> =
        combine(
            snapshotUseCase(snapshotId).onEach { snapshot ->
                currentSnapshot = snapshot
            },
            isShareFormatDialogVisible,
            shareDraft,
        ) { snapshot, isShareFormatDialogVisible, shareDraft ->
            (snapshot?.toHistoryDetailUiState(timeProvider) ?: HistoryDetailUiState.previewMissing()).copy(
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
            )
        }

    fun deleteSnapshot(
        onDeleted: () -> Unit,
    ) {
        viewModelScope.launch {
            // TODO Remove this test-only delete action when history management UX is finalized.
            deleteSnapshotUseCase(snapshotId)
            onDeleted()
        }
    }

    fun showShareOptions() {
        if (currentSnapshot != null) {
            isShareFormatDialogVisible.value = true
        }
    }

    fun showTextSharePreview() {
        val snapshot = currentSnapshot ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.text(snapshotShareTextFormatter(snapshot))
    }

    fun showFileSharePreview() {
        val snapshot = currentSnapshot ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.file(
            messageText = "Here is the ${snapshot.cadence.historyLabel.lowercase()} routine snapshot from ${snapshot.historyDisplayDate}.",
            fileText = snapshotShareTextFormatter(snapshot),
            fileName = "routine-snapshot-${snapshot.historyDisplayDate}.txt",
        )
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

    @AssistedFactory
    interface Factory {
        fun create(snapshotId: Long): HistoryDetailViewModel
    }
}
