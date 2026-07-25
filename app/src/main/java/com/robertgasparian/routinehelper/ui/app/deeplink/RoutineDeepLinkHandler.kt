package com.robertgasparian.routinehelper.ui.app.deeplink

import androidx.navigation3.runtime.deeplink.DeepLinkRequest

internal fun interface RoutineDeepLinkHandler {
    fun resolve(request: DeepLinkRequest): RoutineNavigationCommand?
}
