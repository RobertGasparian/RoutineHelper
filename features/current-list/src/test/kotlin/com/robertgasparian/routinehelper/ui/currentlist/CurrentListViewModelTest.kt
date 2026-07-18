package com.robertgasparian.routinehelper.ui.currentlist

import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.formatter.CurrentListShareTextFormatter
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.removal.FakeRoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalRequest
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoState
import com.robertgasparian.routinehelper.domain.repository.AddedCurrentListItem
import com.robertgasparian.routinehelper.domain.repository.CurrentListCheckedChange
import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.UpdatedCurrentListItem
import com.robertgasparian.routinehelper.domain.usecase.AddCurrentListItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.CurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderCurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetAllCurrentListItemsCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetCurrentListItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateCurrentListItemUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCurrentListRepository()
    private val removalUndoCoordinator = FakeRoutineRemovalUndoCoordinator()

    @Test
    fun `given items when observing ui state then maps items and share text`() = runTest {
        repository.setItems(
            listOf(
                currentListItem(
                    id = 10L,
                    title = "Pick up dry cleaning",
                    description = "Before 6 PM",
                    isChecked = true,
                ),
            ),
        )
        val viewModel = createViewModel()

        val state = viewModel.uiState.first { it.items.isNotEmpty() }

        assertEquals(
            listOf(
                CurrentListItemUiState(
                    id = 10L,
                    title = "Pick up dry cleaning",
                    description = "Before 6 PM",
                    isChecked = true,
                ),
            ),
            state.items,
        )
        assertEquals(
            """
                Current list

                1. [x] Pick up dry cleaning
                   Description: Before 6 PM
            """.trimIndent(),
            state.shareText,
        )
    }

    @Test
    fun `when item intents are handled then forwards them to use cases`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(CurrentListIntent.AddItem(title = "  Send package  ", description = "  UPS  "))
        viewModel.onIntent(
            CurrentListIntent.UpdateItem(
                itemId = 9L,
                title = "  Send updated package  ",
                description = "  FedEx  ",
            ),
        )
        viewModel.onIntent(CurrentListIntent.CheckedChange(itemId = 10L, isChecked = true))
        viewModel.onIntent(CurrentListIntent.SetAllChecked(isChecked = false))
        viewModel.onIntent(CurrentListIntent.RemoveItem(itemId = 11L))
        viewModel.onIntent(CurrentListIntent.ReorderItems(listOf(11L, 10L)))
        viewModel.onIntent(CurrentListIntent.ClearListConfirm)
        advanceUntilIdle()

        assertEquals(
            listOf(
                AddedCurrentListItem(
                    title = "Send package",
                    description = "UPS",
                ),
            ),
            repository.addedItems,
        )
        assertEquals(
            listOf(
                UpdatedCurrentListItem(
                    itemId = 9L,
                    title = "Send updated package",
                    description = "FedEx",
                ),
            ),
            repository.updatedItems,
        )
        assertEquals(
            listOf(
                CurrentListCheckedChange(
                    itemId = 10L,
                    isChecked = true,
                ),
            ),
            repository.checkedChanges,
        )
        assertEquals(listOf(false), repository.allCheckedChanges)
        assertEquals(
            listOf(RoutineRemovalRequest(RoutineRemovalSource.CurrentList, itemId = 11L)),
            removalUndoCoordinator.removalRequests,
        )
        assertEquals(listOf(listOf(11L, 10L)), repository.reorderedItemIds)
        assertEquals(1, removalUndoCoordinator.clearCurrentListCount)
    }

    @Test
    fun `given daily removals are pending when observing state then current list removal is disabled`() = runTest {
        removalUndoCoordinator.setState(
            RoutineRemovalUndoState(
                activeSource = RoutineRemovalSource.Daily,
                pendingItemCount = 1,
            ),
        )
        val viewModel = createViewModel()

        val state = viewModel.uiState.first { !it.canRemoveItems }

        assertEquals(false, state.canRemoveItems)
    }

    @Test
    fun `given current list has an item when test items are added then appends twenty numbered items`() = runTest {
        repository.setItems(
            listOf(
                currentListItem(
                    id = 1L,
                    title = "Existing item",
                ),
            ),
        )
        val viewModel = createViewModel()
        viewModel.uiState.first { state -> state.items.size == 1 }

        viewModel.onIntent(CurrentListIntent.AddTestItemsClick)
        advanceUntilIdle()

        assertEquals(20, repository.addedItems.size)
        assertEquals(
            AddedCurrentListItem(
                title = "list item 2",
                description = "description for list item 2",
            ),
            repository.addedItems.first(),
        )
        assertEquals(
            AddedCurrentListItem(
                title = "list item 21",
                description = null,
            ),
            repository.addedItems.last(),
        )
    }

    private fun createViewModel(): CurrentListViewModel =
        CurrentListViewModel(
            currentListItemsUseCase = CurrentListItemsUseCase(repository),
            addCurrentListItemUseCase = AddCurrentListItemUseCase(repository),
            updateCurrentListItemUseCase = UpdateCurrentListItemUseCase(repository),
            reorderCurrentListItemsUseCase = ReorderCurrentListItemsUseCase(repository),
            setAllCurrentListItemsCheckedUseCase = SetAllCurrentListItemsCheckedUseCase(repository),
            setCurrentListItemCheckedUseCase = SetCurrentListItemCheckedUseCase(repository),
            currentListShareTextFormatter = CurrentListShareTextFormatter(),
            routineRemovalUndoCoordinator = removalUndoCoordinator,
        )

    private fun currentListItem(
        id: Long,
        title: String,
        description: String? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ): CurrentListItem =
        CurrentListItem(
            id = id,
            title = title,
            description = description,
            position = position,
            isChecked = isChecked,
        )
}
