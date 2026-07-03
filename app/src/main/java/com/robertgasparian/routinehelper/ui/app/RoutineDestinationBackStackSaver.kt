package com.robertgasparian.routinehelper.ui.app

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

internal val RoutineDestinationBackStackSaver: Saver<TopLevelBackStack<RoutineDestination>, Any> =
    listSaver(
        save = { backStack -> saveRoutineDestinationBackStack(backStack) },
        restore = { savedEntries -> restoreRoutineDestinationBackStack(savedEntries) },
    )

internal fun saveRoutineDestinationBackStack(
    backStack: TopLevelBackStack<RoutineDestination>,
): List<List<Any?>> =
    backStack.backStack.map(RoutineDestination::toSaveableRoute)

internal fun restoreRoutineDestinationBackStack(
    savedEntries: List<*>,
): TopLevelBackStack<RoutineDestination> {
    val destinations = mutableListOf<RoutineDestination>()
    for (savedEntry in savedEntries) {
        val destination = (savedEntry as? List<*>)?.toRoutineDestinationOrNull()
            ?: return defaultRoutineDestinationBackStack()
        destinations += destination
    }

    if (destinations.firstOrNull() !is TopLevelDestination) {
        return defaultRoutineDestinationBackStack()
    }

    return TopLevelBackStack.fromRestored(destinations)
}

private fun defaultRoutineDestinationBackStack(): TopLevelBackStack<RoutineDestination> =
    TopLevelBackStack(DailyDestination)

private fun RoutineDestination.toSaveableRoute(): List<Any?> =
    when (this) {
        DailyDestination -> listOf(RouteDaily)
        WeeklyDestination -> listOf(RouteWeekly)
        HistoryDestination -> listOf(RouteHistory)
        SettingsDestination -> listOf(RouteSettings)
        is HistoryDetailDestination -> listOf(RouteHistoryDetail, snapshotId)
        is ActionEditorDestination -> listOf(RouteActionEditor, actionId, cadence.toSaveableValue())
        is ShareTextPreviewDestination -> listOf(RouteShareTextPreview, initialText, shareTitle)
    }

private fun List<*>.toRoutineDestinationOrNull(): RoutineDestination? =
    when (firstOrNull()) {
        RouteDaily -> DailyDestination
        RouteWeekly -> WeeklyDestination
        RouteHistory -> HistoryDestination
        RouteSettings -> SettingsDestination
        RouteHistoryDetail -> HistoryDetailDestination(
            snapshotId = getOrNull(1).asLongOrNull() ?: return null,
        )
        RouteActionEditor -> ActionEditorDestination(
            actionId = getOrNull(1).asLongOrNull(),
            cadence = getOrNull(2).toRoutineCadenceOrNull() ?: return null,
        )
        RouteShareTextPreview -> ShareTextPreviewDestination(
            initialText = getOrNull(1) as? String ?: return null,
            shareTitle = getOrNull(2) as? String ?: return null,
        )
        else -> null
    }

private fun RoutineCadence.toSaveableValue(): String =
    when (this) {
        RoutineCadence.Daily -> CadenceDaily
        RoutineCadence.Weekly -> CadenceWeekly
    }

private fun Any?.toRoutineCadenceOrNull(): RoutineCadence? =
    when (this) {
        CadenceDaily -> RoutineCadence.Daily
        CadenceWeekly -> RoutineCadence.Weekly
        else -> null
    }

private fun Any?.asLongOrNull(): Long? =
    when (this) {
        is Long -> this
        is Int -> toLong()
        else -> null
    }

private const val RouteDaily = "daily"
private const val RouteWeekly = "weekly"
private const val RouteHistory = "history"
private const val RouteSettings = "settings"
private const val RouteHistoryDetail = "history_detail"
private const val RouteActionEditor = "action_editor"
private const val RouteShareTextPreview = "share_text_preview"

private const val CadenceDaily = "daily"
private const val CadenceWeekly = "weekly"
