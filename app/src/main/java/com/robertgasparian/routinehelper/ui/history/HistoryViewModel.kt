package com.robertgasparian.routinehelper.ui.history

import androidx.lifecycle.ViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

@HiltViewModel
class HistoryViewModel @Inject constructor(
    snapshotSummariesUseCase: SnapshotSummariesUseCase,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> =
        snapshotSummariesUseCase()
            .map { summaries ->
                HistoryUiState(
                    snapshots = summaries.map(RoutineDaySummary::toUiState),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState(),
            )
}

private fun RoutineDaySummary.toUiState(): HistorySnapshotUiState =
    HistorySnapshotUiState(
        snapshotId = snapshotId,
        date = date,
        finalizedLabel = "Finalized ${timeFormatter.format(Instant.ofEpochMilli(finalizedAtMillis))}",
    )

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
