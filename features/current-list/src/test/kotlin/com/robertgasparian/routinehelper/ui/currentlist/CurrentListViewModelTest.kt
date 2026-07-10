package com.robertgasparian.routinehelper.ui.currentlist

import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.formatter.CurrentListShareTextFormatter
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.repository.AddedCurrentListItem
import com.robertgasparian.routinehelper.domain.repository.CurrentListCheckedChange
import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.usecase.AddCurrentListItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.ClearCurrentListUseCase
import com.robertgasparian.routinehelper.domain.usecase.CurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderCurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemsPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetAllCurrentListItemsCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetCurrentListItemCheckedUseCase
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoCoordinator
import kotlinx.coroutines.CoroutineScope
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
                CurrentListCheckedChange(
                    itemId = 10L,
                    isChecked = true,
                ),
            ),
            repository.checkedChanges,
        )
        assertEquals(listOf(false), repository.allCheckedChanges)
        assertEquals(listOf(11L), repository.pendingRemovalItemIds)
        assertEquals(emptyList<List<Long>>(), repository.deletedPendingRemovalItemIdGroups)
        assertEquals(listOf(listOf(11L, 10L)), repository.reorderedItemIds)
        assertEquals(1, repository.clearCount)
    }

    private fun createViewModel(): CurrentListViewModel =
        CurrentListViewModel(
            currentListItemsUseCase = CurrentListItemsUseCase(repository),
            addCurrentListItemUseCase = AddCurrentListItemUseCase(repository),
            reorderCurrentListItemsUseCase = ReorderCurrentListItemsUseCase(repository),
            setAllCurrentListItemsCheckedUseCase = SetAllCurrentListItemsCheckedUseCase(repository),
            setCurrentListItemCheckedUseCase = SetCurrentListItemCheckedUseCase(repository),
            currentListShareTextFormatter = CurrentListShareTextFormatter(),
            currentListUndoCoordinator = createCurrentListUndoCoordinator(),
        )

    private fun createCurrentListUndoCoordinator(): CurrentListUndoCoordinator =
        CurrentListUndoCoordinator(
            markCurrentListItemPendingRemovalUseCase = MarkCurrentListItemPendingRemovalUseCase(repository),
            restoreCurrentListItemPendingRemovalUseCase = RestoreCurrentListItemPendingRemovalUseCase(repository),
            restoreCurrentListItemsPendingRemovalUseCase = RestoreCurrentListItemsPendingRemovalUseCase(repository),
            deleteCurrentListPendingRemovalsUseCase = DeleteCurrentListPendingRemovalsUseCase(repository),
            deleteAllCurrentListPendingRemovalsUseCase = DeleteAllCurrentListPendingRemovalsUseCase(repository),
            clearCurrentListUseCase = ClearCurrentListUseCase(repository),
            coroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher),
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
