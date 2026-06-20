package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState

internal fun WeeklyRoutineItem.toRoutineTrackingItemUiState(): RoutineTrackingItemUiState =
    RoutineTrackingItemUiState(
        routineItemId = routineItemId,
        actionId = actionId,
        title = title,
        description = description,
        repeatTargetCount = repeatTargetCount,
        completedCount = completedCount,
        isChecked = isChecked,
        isHidden = isHidden,
        note = note.orEmpty(),
    )
