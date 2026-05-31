package com.robertgasparian.routinehelper.ui.history.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotShareTextUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    @Assisted private val snapshotId: Long,
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val snapshotShareTextUseCase: SnapshotShareTextUseCase,
    private val snapshotUseCase: SnapshotUseCase,
) : ViewModel() {
    private val isShareFormatDialogVisible = MutableStateFlow(false)
    private val shareDraft = MutableStateFlow<ShareDraft?>(null)
    private var currentSnapshot: RoutineDaySnapshot? = null

    val uiState: Flow<HistoryDetailUiState> =
        combine(
            snapshotUseCase(snapshotId).onEach { snapshot ->
                currentSnapshot = snapshot
            },
            isShareFormatDialogVisible,
            shareDraft,
        ) { snapshot, isShareFormatDialogVisible, shareDraft ->
            (snapshot?.toUiState() ?: HistoryDetailUiState.previewMissing()).copy(
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
        shareDraft.value = ShareDraft.text(snapshotShareTextUseCase(snapshot))
    }

    fun showFileSharePreview() {
        val snapshot = currentSnapshot ?: return
        isShareFormatDialogVisible.value = false
        shareDraft.value = ShareDraft.file(
            messageText = "Here is the ${snapshot.cadence.label.lowercase()} routine snapshot from ${snapshot.displayDate}.",
            fileText = snapshotShareTextUseCase(snapshot),
        )
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

private fun RoutineDaySnapshot.toUiState(): HistoryDetailUiState =
    HistoryDetailUiState(
        date = displayDate,
        finalizedLabel = "Finalized ${timeFormatter.format(Instant.ofEpochMilli(finalizedAtMillis))}",
        summaryNote = summaryNote.orEmpty(),
        items = items.map(RoutineDaySnapshotItem::toUiState),
    )

private val RoutineDaySnapshot.displayDate: String
    get() = if (cadence == RoutineCadence.Weekly) "Week of $date" else date

private val RoutineCadence.label: String
    get() = when (this) {
        RoutineCadence.Daily -> "Daily"
        RoutineCadence.Weekly -> "Weekly"
    }

private fun RoutineDaySnapshotItem.toUiState(): HistoryDetailItemUiState =
    HistoryDetailItemUiState(
        actionId = actionId,
        title = title,
        description = description,
        repeatTargetCount = repeatTargetCount,
        completedCount = completedCount,
        isChecked = isChecked,
        note = note,
    )

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
