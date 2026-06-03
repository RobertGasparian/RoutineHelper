package com.robertgasparian.routinehelper.ui.app

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

sealed interface TopLevelDestination

data object DailyDestination : TopLevelDestination

data object WeeklyDestination : TopLevelDestination

data object HistoryDestination : TopLevelDestination

data class HistoryDetailDestination(
    val snapshotId: Long,
)

data class ActionEditorDestination(
    val actionId: Long? = null,
    val cadence: RoutineCadence = RoutineCadence.Daily,
)
