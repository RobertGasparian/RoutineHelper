package com.robertgasparian.routinehelper.ui.app.deeplink

import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import com.robertgasparian.routinehelper.ui.app.DailyDestination
import com.robertgasparian.routinehelper.ui.app.WeeklyDestination
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class RoutineTabDeepLinkHandler @Inject constructor() : RoutineDeepLinkHandler {
    private val matcher = UriDeepLinkMatcher(
        uriPattern = RoutineDeepLinks.ROUTINE_PATTERN.toUri(),
        serializer = RoutineTabDeepLinkKey.serializer(),
    )

    override fun resolve(request: DeepLinkRequest): RoutineNavigationCommand? {
        val key = matcher.match(request)?.key ?: return null
        val destination = when (key.cadence) {
            RoutineTabDeepLinkCadence.Daily -> DailyDestination
            RoutineTabDeepLinkCadence.Weekly -> WeeklyDestination
        }
        return RoutineNavigationCommand(topLevelDestination = destination)
    }
}

/**
 * External URI model only. Keeping it separate from the app's cadence and destination types lets
 * the public link vocabulary evolve without turning internal presentation state into URI state.
 */
@Serializable
private data class RoutineTabDeepLinkKey(
    val cadence: RoutineTabDeepLinkCadence,
) : NavKey

@Serializable
private enum class RoutineTabDeepLinkCadence {
    @SerialName("daily")
    Daily,

    @SerialName("weekly")
    Weekly,
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoutineTabDeepLinkModule {
    @Binds
    @IntoSet
    abstract fun bindRoutineTabDeepLinkHandler(
        handler: RoutineTabDeepLinkHandler,
    ): RoutineDeepLinkHandler
}
