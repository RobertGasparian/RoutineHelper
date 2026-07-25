package com.robertgasparian.routinehelper.ui.app.deeplink

import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RoutineDeepLinkRegistry @Inject constructor(
    private val handlers: Set<@JvmSuppressWildcards RoutineDeepLinkHandler>,
) {
    /**
     * Resolves every source through the same URI-only boundary.
     *
     * Android Intent actions, notification metadata, and whether a link was explicit or implicit
     * are deliberately excluded. Those concerns only deliver the string to this registry.
     */
    fun resolve(uriString: String?): RoutineNavigationCommand? {
        val requestUri = uriString?.takeIf(String::isNotBlank) ?: return null
        return try {
            val request = DeepLinkRequest(requestUri)
            handlers.mapNotNull { handler -> handler.resolve(request) }.singleOrNull()
        } catch (_: Exception) {
            // Deep links are untrusted input. Malformed or ambiguous links fall back to the
            // currently restored/default navigation state instead of crashing the app.
            null
        }
    }
}
