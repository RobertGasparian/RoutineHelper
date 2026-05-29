package com.robertgasparian.routinehelper.ui.app

sealed interface TopLevelDestination

data object TodayDestination : TopLevelDestination

data object HistoryDestination : TopLevelDestination

data class HistoryDetailDestination(
    val snapshotId: Long,
)

data class ActionEditorDestination(
    val actionId: Long? = null,
)
