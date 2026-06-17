package com.robertgasparian.routinehelper.ui.daily

import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState

internal fun TodayRoutineItem.toRoutineTrackingItemUiState(): RoutineTrackingItemUiState =
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
