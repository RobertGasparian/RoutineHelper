package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary

internal fun RoutineSnapshotSummary.toHistorySnapshotUiState(isSelected: Boolean): HistorySnapshotUiState =
    HistorySnapshotUiState(
        snapshotId = snapshotId,
        date = periodStartDate,
        cadence = cadence,
        completedCount = completedCount,
        totalCount = totalCount,
        hasSummaryNote = hasSummaryNote,
        isSelected = isSelected,
    )
