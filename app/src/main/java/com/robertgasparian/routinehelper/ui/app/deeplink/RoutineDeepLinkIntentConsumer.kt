package com.robertgasparian.routinehelper.ui.app.deeplink

import android.content.Intent
import javax.inject.Inject

/**
 * Consumes the URI carried by one Android intent exactly once.
 *
 * Clearing the URI after parsing lets [com.robertgasparian.routinehelper.MainActivity] inspect
 * every `onCreate` intent, including a new intent that restores an existing task, without replaying
 * the same navigation request when Android recreates the activity.
 */
internal class RoutineDeepLinkIntentConsumer @Inject constructor(
    private val registry: RoutineDeepLinkRegistry,
) {
    fun consume(intent: Intent): RoutineNavigationCommand? {
        val command = registry.resolve(intent.dataString) ?: return null
        intent.data = null
        return command
    }
}
