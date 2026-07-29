package com.robertgasparian.routinehelper.ui.app.deeplink

import android.app.Application
import android.content.Intent
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.app.DailyDestination
import com.robertgasparian.routinehelper.ui.app.HistoryDestination
import com.robertgasparian.routinehelper.ui.app.HistoryDetailDestination
import com.robertgasparian.routinehelper.ui.app.ReflectionEditorDestination
import com.robertgasparian.routinehelper.ui.app.WeeklyDestination
import com.robertgasparian.routinehelper.ui.app.historyDetailNavigationContentKey
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailInitialAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class RoutineDeepLinkRegistryTest {
    private val registry = RoutineDeepLinkRegistry(
        handlers = setOf(
            HistorySummaryEditDeepLinkHandler(),
            RoutineTabDeepLinkHandler(),
        ),
    )
    private val intentConsumer = RoutineDeepLinkIntentConsumer(registry)

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
                    ReflectionEditorDestination(
                        parentContentKey = historyDetailNavigationContentKey(snapshotId = 42L),
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
    fun `given daily routine URI when resolving then returns the Daily root path`() {
        assertEquals(
            RoutineNavigationCommand(topLevelDestination = DailyDestination),
            registry.resolve(RoutineDeepLinks.routine(RoutineCadence.Daily).toString()),
        )
    }

    @Test
    fun `given weekly routine URI when resolving then returns the Weekly root path`() {
        assertEquals(
            RoutineNavigationCommand(topLevelDestination = WeeklyDestination),
            registry.resolve(RoutineDeepLinks.routine(RoutineCadence.Weekly).toString()),
        )
    }

    @Test
    fun `given unsupported routine cadence when resolving then returns no command`() {
        assertNull(registry.resolve("routinehelper://routines/monthly"))
    }

    @Test
    fun `given unsupported URI when resolving then returns no command`() {
        assertNull(registry.resolve("routinehelper://settings"))
    }

    @Test
    fun `given a supported intent when consuming twice then navigation is emitted only once`() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            RoutineDeepLinks.historySummaryEdit(snapshotId = 42L),
        )

        assertNotNull(intentConsumer.consume(intent))
        assertNull(intentConsumer.consume(intent))
        assertNull(intent.data)
    }

    @Test
    fun `given separate intents with the same URI when consuming then both are accepted`() {
        val uri = RoutineDeepLinks.historySummaryEdit(snapshotId = 42L)

        assertNotNull(intentConsumer.consume(Intent(Intent.ACTION_VIEW, uri)))
        assertNotNull(intentConsumer.consume(Intent(Intent.ACTION_VIEW, uri)))
    }
}
