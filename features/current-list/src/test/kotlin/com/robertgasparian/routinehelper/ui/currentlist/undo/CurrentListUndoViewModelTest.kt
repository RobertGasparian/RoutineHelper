package com.robertgasparian.routinehelper.ui.currentlist.undo

import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.usecase.ClearCurrentListUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemsPendingRemovalUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentListUndoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCurrentListRepository()

    @Test
    fun `given pending removals when undo latest intent is handled then restores latest item`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        coordinator.requestRemoval(itemId = 11L)
        val viewModel = CurrentListUndoViewModel(coordinator)

        viewModel.onIntent(CurrentListUndoIntent.UndoLatestClick)
        runCurrent()

        assertEquals(listOf(11L), repository.restoredPendingRemovalItemIds)
        assertEquals(CurrentListUndoUiState(pendingItemCount = 1), viewModel.uiState.value)
    }

    @Test
    fun `given pending removals when undo all intent is handled then restores every item`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        coordinator.requestRemoval(itemId = 11L)
        val viewModel = CurrentListUndoViewModel(coordinator)

        viewModel.onIntent(CurrentListUndoIntent.UndoAllClick)
        runCurrent()

        assertEquals(listOf(listOf(10L, 11L)), repository.restoredPendingRemovalItemIdGroups)
        assertEquals(CurrentListUndoUiState(), viewModel.uiState.value)
    }

    private fun TestScope.createCoordinator(): CurrentListUndoCoordinator =
        CurrentListUndoCoordinator(
            markCurrentListItemPendingRemovalUseCase = MarkCurrentListItemPendingRemovalUseCase(repository),
            restoreCurrentListItemPendingRemovalUseCase = RestoreCurrentListItemPendingRemovalUseCase(repository),
            restoreCurrentListItemsPendingRemovalUseCase = RestoreCurrentListItemsPendingRemovalUseCase(repository),
            deleteCurrentListPendingRemovalsUseCase = DeleteCurrentListPendingRemovalsUseCase(repository),
            deleteAllCurrentListPendingRemovalsUseCase = DeleteAllCurrentListPendingRemovalsUseCase(repository),
            clearCurrentListUseCase = ClearCurrentListUseCase(repository),
            coroutineScope = this,
        )
}
