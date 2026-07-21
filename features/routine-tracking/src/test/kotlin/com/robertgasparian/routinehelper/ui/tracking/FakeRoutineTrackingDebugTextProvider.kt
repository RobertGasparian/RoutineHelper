package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

internal val englishDebugTextProvider = RoutineTrackingDebugTextProvider { cadence, itemNumber ->
    val actionLabel = when (cadence) {
        RoutineCadence.Daily -> "daily action"
        RoutineCadence.Weekly -> "weekly action"
    }
    RoutineTrackingDebugItemText(
        title = "$actionLabel $itemNumber",
        description = "description for $actionLabel $itemNumber",
    )
}
