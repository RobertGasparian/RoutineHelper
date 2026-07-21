package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.ui.history.HistoryTextProvider
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    @Assisted private val snapshotId: Long,
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val historyTextProvider: HistoryTextProvider,
    snapshotUseCase: SnapshotUseCase,
) : BaseViewModel<HistoryDetailUiState, HistoryDetailIntent, HistoryDetailUiEvent>() {
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)

    private val snapshot: StateFlow<RoutineSnapshot?> =
        snapshotUseCase(snapshotId).stateInViewModel(initialValue = null)

    override val uiState: StateFlow<HistoryDetailUiState> =
        combine(
            snapshot,
            isShareFormatDialogVisible,
            shareDraft,
        ) { snapshot, isShareFormatDialogVisible, shareDraft ->
            (snapshot?.toHistoryDetailUiState(
                finalizedTime = historyTextProvider.finalizedTime(snapshot.finalizedAtMillis),
            ) ?: HistoryDetailUiState.previewMissing()).copy(
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
            )
        }
            .stateInViewModel(initialValue = HistoryDetailUiState.previewMissing())

    override fun handleIntent(intent: HistoryDetailIntent) {
        when (intent) {
            HistoryDetailIntent.BackClick,
            is HistoryDetailIntent.ShareFileConfirm,
            is HistoryDetailIntent.ShareTextConfirm -> Unit
            HistoryDetailIntent.DeleteClick -> deleteSnapshot()
            HistoryDetailIntent.ShareAsFileClick -> showFileSharePreview()
            HistoryDetailIntent.ShareAsTextClick -> showTextSharePreview()
            HistoryDetailIntent.ShareClick -> showShareOptions()
            HistoryDetailIntent.ShareDismiss -> dismissSharePreview()
            is HistoryDetailIntent.ShareFileNameChange -> updateShareFileName(intent.fileName)
            is HistoryDetailIntent.ShareTextChange -> updateShareText(intent.text)
        }
    }

    private fun deleteSnapshot() {
        launch {
            // TODO Remove this test-only delete action when history management UX is finalized.
            deleteSnapshotUseCase(snapshotId)
            emitUiEvent(HistoryDetailUiEvent.SnapshotDeleted)
        }
    }

    private fun showShareOptions() {
        if (snapshot.value != null) {
            isShareFormatDialogVisible.value = true
        }
    }

    private fun showTextSharePreview() {
        val snapshot = snapshot.value ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.text(historyTextProvider.snapshotShareText(snapshot))
    }

    private fun showFileSharePreview() {
        val snapshot = snapshot.value ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.file(
            messageText = historyTextProvider.snapshotFileMessage(snapshot),
            fileText = historyTextProvider.snapshotShareText(snapshot),
            fileName = historyTextProvider.snapshotFileName(snapshot),
        )
    }

    private fun updateShareFileName(fileName: String) {
        shareDraft.value = when (val draft = shareDraft.value) {
            is ShareDraft.File -> draft.copy(fileName = fileName)
            is ShareDraft.Text,
            null -> draft
        }
    }

    private fun updateShareText(text: String) {
        shareDraft.value = when (val draft = shareDraft.value) {
            is ShareDraft.File -> draft.copy(messageText = text)
            is ShareDraft.Text -> draft.copy(messageText = text)
            null -> null
        }
    }

    private fun dismissSharePreview() {
        isShareFormatDialogVisible.value = false
        shareDraft.value = null
    }

    @AssistedFactory
    interface Factory {
        fun create(snapshotId: Long): HistoryDetailViewModel
    }
}
