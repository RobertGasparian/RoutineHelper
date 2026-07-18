package com.robertgasparian.routinehelper.domain.removal

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.TemplateItemRemoval
import com.robertgasparian.routinehelper.domain.repository.TemplateItemRemovalGroup
import com.robertgasparian.routinehelper.domain.usecase.ClearCurrentListUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllTemplatePendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteTemplatePendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkTemplateItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemsPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreTemplateItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreTemplateItemsPendingRemovalUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoutineRemovalUndoCoordinatorTest {
    private val currentListRepository = FakeCurrentListRepository()
    private val templateRepository = FakeRoutineTemplateRepository()

    @Test
    fun `when daily removal is requested then marks item pending and publishes daily state`() = runTest {
        val coordinator = createCoordinator()

        val accepted = coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 10L)

        assertTrue(accepted)
        assertEquals(
            listOf(TemplateItemRemoval(RoutineCadence.Daily, routineItemId = 10L)),
            templateRepository.pendingRemovalItems,
        )
        assertEquals(
            RoutineRemovalUndoState(
                activeSource = RoutineRemovalSource.Daily,
                pendingItemCount = 1,
            ),
            coordinator.state.value,
        )
    }

    @Test
    fun `given daily removal is pending when weekly removal is requested then rejects other source`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 10L)

        val accepted = coordinator.requestRemoval(RoutineRemovalSource.Weekly, itemId = 20L)

        assertFalse(accepted)
        assertEquals(1, templateRepository.pendingRemovalItems.size)
        assertEquals(RoutineRemovalSource.Daily, coordinator.state.value.activeSource)
    }

    @Test
    fun `given daily removals when undo all then restores the full daily group and unlocks sources`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 10L)
        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 11L)

        coordinator.undoAll()

        assertEquals(
            listOf(
                TemplateItemRemovalGroup(
                    cadence = RoutineCadence.Daily,
                    routineItemIds = listOf(10L, 11L),
                ),
            ),
            templateRepository.restoredPendingRemovalItemGroups,
        )
        assertEquals(RoutineRemovalUndoState(), coordinator.state.value)
    }

    @Test
    fun `given weekly removals when timeout elapses then deletes weekly group and unlocks sources`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.Weekly, itemId = 20L)
        coordinator.requestRemoval(RoutineRemovalSource.Weekly, itemId = 21L)

        advanceTimeBy(4_000L)
        advanceUntilIdle()

        assertEquals(
            listOf(
                TemplateItemRemovalGroup(
                    cadence = RoutineCadence.Weekly,
                    routineItemIds = listOf(20L, 21L),
                ),
            ),
            templateRepository.deletedPendingRemovalItemGroups,
        )
        assertEquals(RoutineRemovalUndoState(), coordinator.state.value)
    }

    @Test
    fun `given pending removal when another same source item is removed then resets timeout`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 10L)
        advanceTimeBy(3_999L)

        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 11L)
        advanceTimeBy(3_999L)

        assertEquals(emptyList<TemplateItemRemovalGroup>(), templateRepository.deletedPendingRemovalItemGroups)

        advanceTimeBy(1L)
        advanceUntilIdle()
        assertEquals(1, templateRepository.deletedPendingRemovalItemGroups.size)
    }

    @Test
    fun `given current list removals when undo latest then restores only latest and keeps group active`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.CurrentList, itemId = 10L)
        coordinator.requestRemoval(RoutineRemovalSource.CurrentList, itemId = 11L)

        coordinator.undoLatest()

        assertEquals(listOf(11L), currentListRepository.restoredPendingRemovalItemIds)
        assertEquals(
            RoutineRemovalUndoState(
                activeSource = RoutineRemovalSource.CurrentList,
                pendingItemCount = 1,
            ),
            coordinator.state.value,
        )
    }

    @Test
    fun `given daily removal is pending when current list clear is requested then rejects clear`() = runTest {
        val coordinator = createCoordinator()
        coordinator.requestRemoval(RoutineRemovalSource.Daily, itemId = 10L)

        val accepted = coordinator.clearCurrentList()

        assertFalse(accepted)
        assertEquals(0, currentListRepository.clearCount)
    }

    @Test
    fun `given no in memory queue when launch cleanup runs then finalizes both durable stores`() = runTest {
        val coordinator = createCoordinator()

        coordinator.finalizeDanglingPendingRemovalsOnLaunch()

        assertEquals(1, currentListRepository.deleteAllPendingRemovalsCount)
        assertEquals(1, templateRepository.deleteAllPendingRemovalsCount)
    }

    @Test
    fun `given timeout elapses during undo when restore completes then item is not deleted`() = runTest {
        val blockingRepository = BlockingRestoreCurrentListRepository()
        val coordinator = createCoordinator(currentListRepository = blockingRepository)
        coordinator.requestRemoval(RoutineRemovalSource.CurrentList, itemId = 10L)

        val undoJob = launch { coordinator.undoLatest() }
        blockingRepository.restoreStarted.await()
        advanceTimeBy(4_000L)

        assertEquals(
            emptyList<List<Long>>(),
            blockingRepository.delegate.deletedPendingRemovalItemIdGroups,
        )

        blockingRepository.allowRestore.complete(Unit)
        undoJob.join()
        advanceUntilIdle()

        assertEquals(listOf(10L), blockingRepository.delegate.restoredPendingRemovalItemIds)
        assertEquals(
            emptyList<List<Long>>(),
            blockingRepository.delegate.deletedPendingRemovalItemIdGroups,
        )
    }

    private fun TestScope.createCoordinator(
        currentListRepository: CurrentListRepository = this@DefaultRoutineRemovalUndoCoordinatorTest.currentListRepository,
    ): DefaultRoutineRemovalUndoCoordinator =
        DefaultRoutineRemovalUndoCoordinator(
            markCurrentListItemPendingRemovalUseCase =
                MarkCurrentListItemPendingRemovalUseCase(currentListRepository),
            restoreCurrentListItemPendingRemovalUseCase =
                RestoreCurrentListItemPendingRemovalUseCase(currentListRepository),
            restoreCurrentListItemsPendingRemovalUseCase =
                RestoreCurrentListItemsPendingRemovalUseCase(currentListRepository),
            deleteCurrentListPendingRemovalsUseCase =
                DeleteCurrentListPendingRemovalsUseCase(currentListRepository),
            deleteAllCurrentListPendingRemovalsUseCase =
                DeleteAllCurrentListPendingRemovalsUseCase(currentListRepository),
            clearCurrentListUseCase = ClearCurrentListUseCase(currentListRepository),
            markTemplateItemPendingRemovalUseCase = MarkTemplateItemPendingRemovalUseCase(templateRepository),
            restoreTemplateItemPendingRemovalUseCase = RestoreTemplateItemPendingRemovalUseCase(templateRepository),
            restoreTemplateItemsPendingRemovalUseCase = RestoreTemplateItemsPendingRemovalUseCase(templateRepository),
            deleteTemplatePendingRemovalsUseCase = DeleteTemplatePendingRemovalsUseCase(templateRepository),
            deleteAllTemplatePendingRemovalsUseCase = DeleteAllTemplatePendingRemovalsUseCase(templateRepository),
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
