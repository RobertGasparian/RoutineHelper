package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun RoutineSnapshot.toHistoryDetailUiState(timeProvider: TimeProvider): HistoryDetailUiState =
    HistoryDetailUiState(
        date = historyDisplayDate,
        cadence = cadence,
        finalizedLabel = "Finalized ${
            historyTimeFormatter
                .withZone(timeProvider.now().zone)
                .format(Instant.ofEpochMilli(finalizedAtMillis))
        }",
        summaryNote = summaryNote.orEmpty(),
        items = items.map(RoutineSnapshotItem::toHistoryDetailItemUiState),
    )

internal val RoutineSnapshot.historyDisplayDate: String
    get() = if (cadence == RoutineCadence.Weekly) "Week of $periodStartDate" else periodStartDate

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

private val historyTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
