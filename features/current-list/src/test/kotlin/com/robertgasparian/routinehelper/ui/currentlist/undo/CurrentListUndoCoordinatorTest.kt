package com.robertgasparian.routinehelper.ui.currentlist.undo

import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import com.robertgasparian.routinehelper.domain.usecase.ClearCurrentListUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemsPendingRemovalUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentListUndoCoordinatorTest {
    private val repository = FakeCurrentListRepository()

    @Test
    fun `when removal is requested then marks item pending and shows snackbar state`() = runTest {
        val coordinator = createCoordinator()

        coordinator.requestRemoval(itemId = 10L)

        assertEquals(listOf(10L), repository.pendingRemovalItemIds)
        assertEquals(
            CurrentListUndoUiState(pendingItemCount = 1),
            coordinator.uiState.value,
        )
    }

    @Test
    fun `given pending removals when timer elapses then deletes pending items and hides snackbar`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        coordinator.requestRemoval(itemId = 11L)

        advanceTimeBy(4_000L)
        advanceUntilIdle()

        assertEquals(listOf(listOf(10L, 11L)), repository.deletedPendingRemovalItemIdGroups)
        assertEquals(CurrentListUndoUiState(), coordinator.uiState.value)
    }

    @Test
    fun `given pending removal when another item is removed then resets timer`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        advanceTimeBy(3_999L)

        coordinator.requestRemoval(itemId = 11L)
        advanceTimeBy(3_999L)

        assertEquals(emptyList<List<Long>>(), repository.deletedPendingRemovalItemIdGroups)

        advanceTimeBy(1L)
        advanceUntilIdle()

        assertEquals(listOf(listOf(10L, 11L)), repository.deletedPendingRemovalItemIdGroups)
    }

    @Test
    fun `given pending removals when undo latest then restores latest item and resets timer`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        coordinator.requestRemoval(itemId = 11L)

        coordinator.undoLatest()

        assertEquals(listOf(11L), repository.restoredPendingRemovalItemIds)
        assertEquals(CurrentListUndoUiState(pendingItemCount = 1), coordinator.uiState.value)

        advanceTimeBy(4_000L)
        advanceUntilIdle()

        assertEquals(listOf(listOf(10L)), repository.deletedPendingRemovalItemIdGroups)
    }

    @Test
    fun `given pending removals when undo all then restores all items and hides snackbar`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)
        coordinator.requestRemoval(itemId = 11L)

        coordinator.undoAll()

        assertEquals(listOf(listOf(10L, 11L)), repository.restoredPendingRemovalItemIdGroups)
        assertEquals(CurrentListUndoUiState(), coordinator.uiState.value)
        advanceTimeBy(4_000L)
        advanceUntilIdle()
        assertEquals(emptyList<List<Long>>(), repository.deletedPendingRemovalItemIdGroups)
    }

    @Test
    fun `given no in memory queue when launch sync runs then deletes dangling pending rows`() = runTest {
        val coordinator = createCoordinator()

        coordinator.finalizeDanglingPendingRemovalsOnLaunch()

        assertEquals(1, repository.deleteAllPendingRemovalsCount)
    }

    @Test
    fun `given pending removals when list is cleared then clears items and cancels pending deletion`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(itemId = 10L)

        coordinator.clearList()
        advanceTimeBy(4_000L)
        advanceUntilIdle()

        assertEquals(1, repository.clearCount)
        assertEquals(emptyList<List<Long>>(), repository.deletedPendingRemovalItemIdGroups)
        assertEquals(CurrentListUndoUiState(), coordinator.uiState.value)
    }

    @Test
    fun `given timeout elapses during undo when restore completes then item is not deleted`() = runTest {
        val blockingRepository = BlockingRestoreCurrentListRepository()
        val coordinator = createCoordinator(blockingRepository)
        coordinator.requestRemoval(itemId = 10L)

        val undoJob = launch { coordinator.undoLatest() }
        blockingRepository.restoreStarted.await()
        advanceTimeBy(4_000L)

        assertEquals(emptyList<List<Long>>(), blockingRepository.delegate.deletedPendingRemovalItemIdGroups)

        blockingRepository.allowRestore.complete(Unit)
        undoJob.join()
        advanceUntilIdle()

        assertEquals(listOf(10L), blockingRepository.delegate.restoredPendingRemovalItemIds)
        assertEquals(emptyList<List<Long>>(), blockingRepository.delegate.deletedPendingRemovalItemIdGroups)
    }

    private fun TestScope.createCoordinator(
        repository: CurrentListRepository = this@CurrentListUndoCoordinatorTest.repository,
    ): CurrentListUndoCoordinator =
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

private class BlockingRestoreCurrentListRepository(
    val delegate: FakeCurrentListRepository = FakeCurrentListRepository(),
) : CurrentListRepository by delegate {
    val restoreStarted = CompletableDeferred<Unit>()
    val allowRestore = CompletableDeferred<Unit>()

    override suspend fun restorePendingRemoval(itemId: Long) {
        restoreStarted.complete(Unit)
        allowRestore.await()
        delegate.restorePendingRemoval(itemId)
    }
}
