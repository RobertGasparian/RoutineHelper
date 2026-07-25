package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateSnapshotSummaryNoteUseCase
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDraftUiState
import com.robertgasparian.routinehelper.ui.history.HistoryTextProvider
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

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
    private val summaryNoteEditor = MutableStateFlow<RoutineNoteDraftUiState?>(null)

    private val snapshot: StateFlow<RoutineSnapshot?> =
        snapshotUseCase(snapshotId)
            .onEach { snapshot ->
                if (snapshot?.isSummaryNoteEditable != true) {
                    summaryNoteEditor.value = null
                }
            }
            .stateInViewModel(initialValue = null)

    override val uiState: StateFlow<HistoryDetailUiState> =
        combine(
            snapshot,
            isShareFormatDialogVisible,
            shareDraft,
            summaryNoteEditor,
        ) { snapshot, isShareFormatDialogVisible, shareDraft, summaryNoteEditor ->
            (snapshot?.toHistoryDetailUiState(
                finalizedTime = historyTextProvider.finalizedTime(snapshot.finalizedAtMillis),
            ) ?: HistoryDetailUiState.previewMissing()).copy(
                isShareFormatDialogVisible = isShareFormatDialogVisible,
                shareDraft = shareDraft,
                summaryNoteEditor = summaryNoteEditor.takeIf {
                    snapshot?.isSummaryNoteEditable == true
                },
            )
        }
            .stateInViewModel(initialValue = HistoryDetailUiState.previewMissing())

    override fun handleIntent(intent: HistoryDetailIntent) {
        when (intent) {
            HistoryDetailIntent.BackClick,
            HistoryDetailIntent.DebugSummaryNotificationClick,
            is HistoryDetailIntent.ShareFileConfirm,
            is HistoryDetailIntent.ShareTextConfirm -> Unit
            HistoryDetailIntent.DeleteClick -> deleteSnapshot()
            HistoryDetailIntent.EditSummaryNoteClick -> showSummaryNoteEditor()
            HistoryDetailIntent.ShareAsFileClick -> showFileSharePreview()
            HistoryDetailIntent.ShareAsTextClick -> showTextSharePreview()
            HistoryDetailIntent.ShareClick -> showShareOptions()
            HistoryDetailIntent.ShareDismiss -> dismissSharePreview()
            is HistoryDetailIntent.ShareFileNameChange -> updateShareFileName(intent.fileName)
            is HistoryDetailIntent.ShareTextChange -> updateShareText(intent.text)
            is HistoryDetailIntent.SummaryNoteDraftChange -> updateSummaryNoteDraft(intent)
            HistoryDetailIntent.SummaryNoteDraftClearClick -> clearSummaryNoteDraft()
            HistoryDetailIntent.SummaryNoteEditorDismiss -> dismissSummaryNoteEditor()
            HistoryDetailIntent.SummaryNoteEditorSaveClick -> saveSummaryNote()
        }
    }

    private fun showSummaryNoteEditor() {
        val snapshot = snapshot.value?.takeIf(RoutineSnapshot::isSummaryNoteEditable) ?: return
        summaryNoteEditor.value = RoutineNoteDraftUiState.fromText(snapshot.summaryNote.orEmpty())
    }

    private fun updateSummaryNoteDraft(intent: HistoryDetailIntent.SummaryNoteDraftChange) {
        summaryNoteEditor.value = summaryNoteEditor.value?.copy(
            text = intent.text,
            selectionStart = intent.selectionStart,
            selectionEnd = intent.selectionEnd,
        )
    }

    private fun clearSummaryNoteDraft() {
        summaryNoteEditor.value = summaryNoteEditor.value?.copy(
            text = "",
            selectionStart = 0,
            selectionEnd = 0,
        )
    }

    private fun dismissSummaryNoteEditor() {
        summaryNoteEditor.value = null
    }

    private fun saveSummaryNote() {
        val editor = summaryNoteEditor.value ?: return
        if (snapshot.value?.isSummaryNoteEditable != true) {
            summaryNoteEditor.value = null
            return
        }

        launch {
            updateSnapshotSummaryNoteUseCase(
                snapshotId = snapshotId,
                summaryNote = editor.text,
            )
            summaryNoteEditor.value = null
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
