package com.robertgasparian.routinehelper.ui.app

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailInitialAction

sealed interface RoutineDestination

sealed interface TopLevelDestination : RoutineDestination

data object DailyDestination : TopLevelDestination

data object WeeklyDestination : TopLevelDestination

data object CurrentListDestination : TopLevelDestination

data object HistoryDestination : TopLevelDestination

data object SettingsDestination : RoutineDestination

data class HistoryDetailDestination(
    val snapshotId: Long,
    val initialAction: HistoryDetailInitialAction? = null,
) : RoutineDestination

data class ActionEditorDestination(
    val actionId: Long? = null,
    val cadence: RoutineCadence = RoutineCadence.Daily,
) : RoutineDestination

data class ShareTextPreviewDestination(
    val initialText: String,
    val shareTitle: String,
) : RoutineDestination
