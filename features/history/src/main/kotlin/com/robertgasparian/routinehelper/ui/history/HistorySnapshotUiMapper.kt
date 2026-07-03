package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary

internal fun RoutineSnapshotSummary.toHistorySnapshotUiState(isSelected: Boolean): HistorySnapshotUiState =
    HistorySnapshotUiState(
        snapshotId = snapshotId,
        date = if (cadence == RoutineCadence.Weekly) "Week of $periodStartDate" else periodStartDate,
        cadence = cadence,
        completedCount = completedCount,
        totalCount = totalCount,
        hasSummaryNote = hasSummaryNote,
        isSelected = isSelected,
    )

internal fun List<RoutineSnapshot>.toHistoryFileShareMessage(): String {
    val periodStartDates = map { snapshot -> snapshot.periodStartDate }.distinct().sorted()
    return when (periodStartDates.size) {
        0 -> "Here are the routine snapshots."
        1 -> "Here are the routine snapshots from ${periodStartDates.first()}."
        else -> "Here are the routine snapshots from ${periodStartDates.first()} to ${periodStartDates.last()}."
    }
}
