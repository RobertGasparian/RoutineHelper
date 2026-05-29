package com.robertgasparian.routinehelper.ui.history.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val deleteSnapshotUseCase: DeleteSnapshotUseCase,
    private val snapshotUseCase: SnapshotUseCase,
) : ViewModel() {
    fun uiState(snapshotId: Long): Flow<HistoryDetailUiState> =
        snapshotUseCase(snapshotId).map { snapshot ->
            snapshot?.toUiState() ?: HistoryDetailUiState.previewMissing()
        }

    fun deleteSnapshot(
        snapshotId: Long,
        onDeleted: () -> Unit,
    ) {
        viewModelScope.launch {
            // TODO Remove this test-only delete action when history management UX is finalized.
            deleteSnapshotUseCase(snapshotId)
            onDeleted()
        }
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
