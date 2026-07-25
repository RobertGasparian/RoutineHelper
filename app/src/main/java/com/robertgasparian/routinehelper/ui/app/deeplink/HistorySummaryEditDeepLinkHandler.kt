package com.robertgasparian.routinehelper.ui.app.deeplink

import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import com.robertgasparian.routinehelper.ui.app.HistoryDestination
import com.robertgasparian.routinehelper.ui.app.HistoryDetailDestination
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailInitialAction
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.serialization.Serializable

internal class HistorySummaryEditDeepLinkHandler @Inject constructor() : RoutineDeepLinkHandler {
    private val matcher = UriDeepLinkMatcher(
        uriPattern = RoutineDeepLinks.HISTORY_SUMMARY_EDIT_PATTERN.toUri(),
        serializer = HistorySummaryEditDeepLinkKey.serializer(),
    )

    override fun resolve(request: DeepLinkRequest): RoutineNavigationCommand? {
        val key = matcher.match(request)?.key?.takeIf { key -> key.snapshotId > 0L } ?: return null
        return RoutineNavigationCommand(
            topLevelDestination = HistoryDestination,
            nestedDestinations = listOf(
                HistoryDetailDestination(
                    snapshotId = key.snapshotId,
                    initialAction = HistoryDetailInitialAction.OpenSummaryEditor,
                ),
            ),
        )
    }
}

/**
 * URI-decoding model only. It is intentionally separate from [HistoryDetailDestination]:
 * the external link contract can evolve without forcing every internal destination to become
 * serializable or exposing presentation-only fields as URI parameters.
 */
@Serializable
private data class HistorySummaryEditDeepLinkKey(
    val snapshotId: Long,
) : NavKey

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HistorySummaryEditDeepLinkModule {
    @Binds
    @IntoSet
    abstract fun bindHistorySummaryEditDeepLinkHandler(
        handler: HistorySummaryEditDeepLinkHandler,
    ): RoutineDeepLinkHandler
}
