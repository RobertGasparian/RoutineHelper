package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

data class HistorySnapshotUiState(
    val snapshotId: Long,
    val date: String,
    val cadence: RoutineCadence,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val hasSummaryNote: Boolean = false,
    val isSelected: Boolean = false,
) {
    val isComplete: Boolean = totalCount > 0 && completedCount == totalCount

    val completionLabel: String = if (totalCount == 0) {
        "No actions saved"
    } else if (isComplete) {
        "All completed!"
    } else {
        "$completedCount/$totalCount completed"
    }
}
