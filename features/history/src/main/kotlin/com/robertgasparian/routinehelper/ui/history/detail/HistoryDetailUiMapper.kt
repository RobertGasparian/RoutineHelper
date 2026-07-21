package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem

internal fun RoutineSnapshot.toHistoryDetailUiState(finalizedTime: String): HistoryDetailUiState =
    HistoryDetailUiState(
        date = historyDisplayDate,
        cadence = cadence,
        finalizedTime = finalizedTime,
        summaryNote = summaryNote.orEmpty(),
        items = items.map(RoutineSnapshotItem::toHistoryDetailItemUiState),
    )

internal val RoutineSnapshot.historyDisplayDate: String
    get() = periodStartDate

private fun RoutineSnapshotItem.toHistoryDetailItemUiState(): HistoryDetailItemUiState =
    HistoryDetailItemUiState(
        actionId = actionId,
        title = title,
        description = description,
        repeatTargetCount = repeatTargetCount,
        completedCount = completedCount,
        isChecked = isChecked,
        isHidden = isHidden,
        note = note,
    )
