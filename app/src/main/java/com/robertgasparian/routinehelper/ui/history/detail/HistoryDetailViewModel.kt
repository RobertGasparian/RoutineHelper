package com.robertgasparian.routinehelper.ui.history.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotShareTextUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
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
    private val shareText = MutableStateFlow<String?>(null)
    private var currentSnapshot: RoutineDaySnapshot? = null

    val uiState: Flow<HistoryDetailUiState> =
        combine(
            snapshotUseCase(snapshotId).onEach { snapshot ->
                currentSnapshot = snapshot
            },
            shareText,
        ) { snapshot, shareText ->
            (snapshot?.toUiState() ?: HistoryDetailUiState.previewMissing()).copy(
                shareText = shareText,
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

    fun showSharePreview() {
        val snapshot = currentSnapshot ?: return
        shareText.value = snapshotShareTextUseCase(snapshot)
    }

    fun updateShareText(text: String) {
        shareText.value = text
    }

    fun dismissSharePreview() {
        shareText.value = null
    }

    @AssistedFactory
    interface Factory {
        fun create(snapshotId: Long): HistoryDetailViewModel
    }
}

private fun RoutineDaySnapshot.toUiState(): HistoryDetailUiState =
    HistoryDetailUiState(
        date = date,
        finalizedLabel = "Finalized ${timeFormatter.format(Instant.ofEpochMilli(finalizedAtMillis))}",
        items = items.map(RoutineDaySnapshotItem::toUiState),
    )

private fun RoutineDaySnapshotItem.toUiState(): HistoryDetailItemUiState =
    HistoryDetailItemUiState(
        actionId = actionId,
        title = title,
        description = description,
        isChecked = isChecked,
        note = note,
    )

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
