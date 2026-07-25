package com.robertgasparian.routinehelper.ui.app.deeplink

import com.robertgasparian.routinehelper.ui.app.RoutineDestination
import com.robertgasparian.routinehelper.ui.app.TopLevelDestination

/**
 * A resolved in-app path that mirrors selecting a top-level tab and navigating from there.
 *
 * Deep-link sources do not choose different stack behavior. Notifications, browser links, and
 * future entry points all resolve to this same command after supplying a URI string.
 */
data class RoutineNavigationCommand(
    val topLevelDestination: TopLevelDestination,
    val nestedDestinations: List<RoutineDestination> = emptyList(),
)
