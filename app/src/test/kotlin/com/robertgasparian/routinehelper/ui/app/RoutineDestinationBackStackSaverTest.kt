package com.robertgasparian.routinehelper.ui.app

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailInitialAction
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineDestinationBackStackSaverTest {
    @Test
    fun `given destination stack when saving and restoring then entries are preserved`() {
        val backStack = TopLevelBackStack<RoutineDestination>(HistoryDestination).apply {
            add(
                HistoryDetailDestination(
                    snapshotId = 42L,
                    initialAction = HistoryDetailInitialAction.OpenSummaryEditor,
                ),
            )
            add(
                ReflectionEditorDestination(
                    parentContentKey = historyDetailNavigationContentKey(snapshotId = 42L),
                ),
            )
            add(
                ShareTextPreviewDestination(
                    initialText = "Snapshot text",
                    shareTitle = "Share routine snapshot",
                ),
            )
        }

        val restored = restoreRoutineDestinationBackStack(
            saveRoutineDestinationBackStack(backStack),
        )

        assertEquals(HistoryDestination, restored.topLevelKey)
        assertEquals(backStack.backStack.toList(), restored.backStack.toList())
    }

    @Test
    fun `given action editor destination when saving and restoring then nullable id and cadence are preserved`() {
        val backStack = TopLevelBackStack<RoutineDestination>(WeeklyDestination).apply {
            add(
                ActionEditorDestination(
                    actionId = null,
                    cadence = RoutineCadence.Weekly,
                ),
            )
        }

        val restored = restoreRoutineDestinationBackStack(
            saveRoutineDestinationBackStack(backStack),
        )

        assertEquals(backStack.backStack.toList(), restored.backStack.toList())
    }

    @Test
    fun `given current list top level destination when saving and restoring then it is preserved`() {
        val backStack = TopLevelBackStack<RoutineDestination>(CurrentListDestination)

        val restored = restoreRoutineDestinationBackStack(
            saveRoutineDestinationBackStack(backStack),
        )

        assertEquals(CurrentListDestination, restored.topLevelKey)
        assertEquals(listOf(CurrentListDestination), restored.backStack.toList())
    }

    @Test
    fun `given malformed saved stack when restoring then daily root is returned`() {
        val restored = restoreRoutineDestinationBackStack(
            listOf(listOf("unknown")),
        )

        assertEquals(DailyDestination, restored.topLevelKey)
        assertEquals(listOf(DailyDestination), restored.backStack.toList())
    }
}
