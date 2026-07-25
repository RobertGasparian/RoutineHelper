package com.robertgasparian.routinehelper.ui.app.deeplink

import com.robertgasparian.routinehelper.ui.app.HistoryDestination
import com.robertgasparian.routinehelper.ui.app.HistoryDetailDestination
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailInitialAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineDeepLinkRegistryTest {
    private val registry = RoutineDeepLinkRegistry(
        handlers = setOf(HistorySummaryEditDeepLinkHandler()),
    )

    @Test
    fun `given history summary URI when resolving then returns the manual History path`() {
        val command = registry.resolve(
            RoutineDeepLinks.historySummaryEdit(snapshotId = 42L).toString(),
        )

        assertEquals(
            RoutineNavigationCommand(
                topLevelDestination = HistoryDestination,
                nestedDestinations = listOf(
                    HistoryDetailDestination(
                        snapshotId = 42L,
                        initialAction = HistoryDetailInitialAction.OpenSummaryEditor,
                    ),
                ),
            ),
            command,
        )
    }

    @Test
    fun `given malformed snapshot ID when resolving then returns no command`() {
        assertNull(
            registry.resolve(
                "routinehelper://history/snapshots/not-a-number/summary/edit",
            ),
        )
    }

    @Test
    fun `given non-positive snapshot ID when resolving then returns no command`() {
        assertNull(
            registry.resolve(
                "routinehelper://history/snapshots/-1/summary/edit",
            ),
        )
    }

    @Test
    fun `given unsupported URI when resolving then returns no command`() {
        assertNull(registry.resolve("routinehelper://settings"))
    }
}
