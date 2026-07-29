package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateSnapshotSummaryNoteUseCase
import com.robertgasparian.routinehelper.ui.history.HistoryTextProvider
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    @Assisted private val snapshotId: Long,
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val historyTextProvider: HistoryTextProvider,
    snapshotUseCase: SnapshotUseCase,
    private val updateSnapshotSummaryNoteUseCase: UpdateSnapshotSummaryNoteUseCase,
) : BaseViewModel<HistoryDetailUiState, HistoryDetailIntent, HistoryDetailUiEvent>() {
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)

    private val snapshot: StateFlow<SnapshotLoadState> =
        snapshotUseCase(snapshotId)
            .map(SnapshotLoadState::Loaded)
            .stateInViewModel(initialValue = SnapshotLoadState.Loading)

    override val uiState: StateFlow<HistoryDetailUiState> =
        combine(
            snapshot,
            isShareFormatDialogVisible,
            shareDraft,
        ) { snapshotLoadState, isShareFormatDialogVisible, shareDraft ->
            val snapshotState = when (snapshotLoadState) {
                SnapshotLoadState.Loading -> HistoryDetailUiState.loading()
                is SnapshotLoadState.Loaded -> snapshotLoadState.snapshot?.toHistoryDetailUiState(
                    finalizedTime = historyTextProvider.finalizedTime(
                        snapshotLoadState.snapshot.finalizedAtMillis,
                    ),
                ) ?: HistoryDetailUiState.previewMissing()
            }
            snapshotState.copy(
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
            )
        }
            .stateInViewModel(initialValue = HistoryDetailUiState.loading())

    override fun handleIntent(intent: HistoryDetailIntent) {
        when (intent) {
            HistoryDetailIntent.BackClick,
            HistoryDetailIntent.DebugSummaryNotificationClick,
            is HistoryDetailIntent.ShareFileConfirm,
            is HistoryDetailIntent.ShareTextConfirm -> Unit
            HistoryDetailIntent.DeleteClick -> deleteSnapshot()
            HistoryDetailIntent.EditSummaryNoteClick -> Unit
            is HistoryDetailIntent.SaveSummaryNote -> saveSummaryNote(intent.note)
            HistoryDetailIntent.ShareAsFileClick -> showFileSharePreview()
            HistoryDetailIntent.ShareAsTextClick -> showTextSharePreview()
            HistoryDetailIntent.ShareClick -> showShareOptions()
            HistoryDetailIntent.ShareDismiss -> dismissSharePreview()
            is HistoryDetailIntent.ShareFileNameChange -> updateShareFileName(intent.fileName)
            is HistoryDetailIntent.ShareTextChange -> updateShareText(intent.text)
        }
    }

    private fun saveSummaryNote(note: String) {
        if (currentSnapshot()?.isSummaryNoteEditable != true) return

        launch {
            updateSnapshotSummaryNoteUseCase(
                snapshotId = snapshotId,
                summaryNote = note,
            )
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
        if (currentSnapshot() != null) {
            isShareFormatDialogVisible.value = true
        }
    }

    private fun showTextSharePreview() {
        val snapshot = currentSnapshot() ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.text(historyTextProvider.snapshotShareText(snapshot))
    }

    private fun showFileSharePreview() {
        val snapshot = currentSnapshot() ?: return
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

    private fun currentSnapshot(): RoutineSnapshot? =
        (snapshot.value as? SnapshotLoadState.Loaded)?.snapshot

    @AssistedFactory
    interface Factory {
        fun create(snapshotId: Long): HistoryDetailViewModel
    }
}

private sealed interface SnapshotLoadState {
    data object Loading : SnapshotLoadState

    data class Loaded(
        val snapshot: RoutineSnapshot?,
    ) : SnapshotLoadState
}
