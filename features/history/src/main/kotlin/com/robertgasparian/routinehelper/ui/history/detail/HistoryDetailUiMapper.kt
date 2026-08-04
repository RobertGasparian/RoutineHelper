package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorTag

internal fun RoutineSnapshot.toHistoryDetailUiState(
    finalizedTime: String,
    cadenceTagTemplate: List<ReflectionTagDefinition> = emptyList(),
): HistoryDetailUiState =
    HistoryDetailUiState(
        date = historyDisplayDate,
        cadence = cadence,
        finalizedTime = finalizedTime,
        summaryNote = summaryNote.orEmpty(),
        rating = rating,
        reflectionTags = when {
            selectedTags.isNotEmpty() -> selectedTags.sortedBy { tag -> tag.position }.map { tag ->
                ReflectionEditorTag(
                    label = tag.label,
                    isSelected = true,
                )
            }
            !isReflectionEditable -> emptyList()
            else -> cadenceTagTemplate.map { tag ->
                ReflectionEditorTag(
                    label = tag.label,
                    isSelected = false,
                )
            }
        },
        isReflectionEditable = isReflectionEditable,
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
