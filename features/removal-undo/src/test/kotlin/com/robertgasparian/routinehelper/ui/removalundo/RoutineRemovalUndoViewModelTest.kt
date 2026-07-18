package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.removal.FakeRoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineRemovalUndoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val coordinator = FakeRoutineRemovalUndoCoordinator()

    @Test
    fun `given coordinator group when observing state then maps source and count`() = runTest {
        val viewModel = RoutineRemovalUndoViewModel(coordinator)
        coordinator.setState(
            RoutineRemovalUndoState(
                activeSource = RoutineRemovalSource.Weekly,
                pendingItemCount = 2,
            ),
        )

        val state = viewModel.uiState.first { it.pendingItemCount == 2 }

        assertEquals(RoutineRemovalSource.Weekly, state.activeSource)
        assertEquals("2 Weekly actions removed", state.message)
    }

    @Test
    fun `when undo intents are handled then forwards them to coordinator`() = runTest {
        val viewModel = RoutineRemovalUndoViewModel(coordinator)

        viewModel.onIntent(RoutineRemovalUndoIntent.UndoLatestClick)
        viewModel.onIntent(RoutineRemovalUndoIntent.UndoAllClick)
        advanceUntilIdle()

        assertEquals(1, coordinator.undoLatestCount)
        assertEquals(1, coordinator.undoAllCount)
    }
}
