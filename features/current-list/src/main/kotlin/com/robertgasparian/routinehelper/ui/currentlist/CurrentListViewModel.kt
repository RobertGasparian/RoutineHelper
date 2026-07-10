package com.robertgasparian.routinehelper.ui.currentlist

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.formatter.CurrentListShareTextFormatter
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.usecase.AddCurrentListItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.CurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderCurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetAllCurrentListItemsCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetCurrentListItemCheckedUseCase
import com.robertgasparian.routinehelper.ui.currentlist.undo.CurrentListUndoCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel
class CurrentListViewModel @Inject constructor(
    currentListItemsUseCase: CurrentListItemsUseCase,
    private val addCurrentListItemUseCase: AddCurrentListItemUseCase,
    private val reorderCurrentListItemsUseCase: ReorderCurrentListItemsUseCase,
    private val setAllCurrentListItemsCheckedUseCase: SetAllCurrentListItemsCheckedUseCase,
    private val setCurrentListItemCheckedUseCase: SetCurrentListItemCheckedUseCase,
    private val currentListShareTextFormatter: CurrentListShareTextFormatter,
    private val currentListUndoCoordinator: CurrentListUndoCoordinator,
) : BaseViewModel<CurrentListUiState, CurrentListIntent, Nothing>() {
    override val uiState: StateFlow<CurrentListUiState> =
        currentListItemsUseCase()
            .map { items ->
                CurrentListUiState(
                    items = items.map(CurrentListItem::toUiState),
                    shareText = currentListShareTextFormatter(items),
                )
            }
            .stateInViewModel(initialValue = CurrentListUiState())

    override fun handleIntent(intent: CurrentListIntent) {
        when (intent) {
            CurrentListIntent.SettingsClick,
            CurrentListIntent.ShareClick -> Unit
            is CurrentListIntent.AddItem -> addItem(
                title = intent.title,
                description = intent.description,
            )
            CurrentListIntent.AddTestItemsClick -> addTestItems()
            is CurrentListIntent.CheckedChange -> setChecked(
                itemId = intent.itemId,
                isChecked = intent.isChecked,
            )
            is CurrentListIntent.SetAllChecked -> setAllChecked(intent.isChecked)
            is CurrentListIntent.RemoveItem -> removeItem(intent.itemId)
            is CurrentListIntent.ReorderItems -> reorderItems(intent.itemIdsInOrder)
            CurrentListIntent.ClearListConfirm -> clearList()
        }
    }

    private fun addTestItems() {
        if (uiState.value.items.isNotEmpty()) return
        launch {
            repeat(TestItemCount) { index ->
                val displayIndex = index + 1
                addCurrentListItemUseCase(
                    title = "list item $displayIndex",
                    description = if (displayIndex % 2 == 0) {
                        "description for list item $displayIndex"
                    } else {
                        null
                    },
                )
            }
        }
    }

    private fun addItem(
        title: String,
        description: String?,
    ) {
        launch {
            addCurrentListItemUseCase(
                title = title,
                description = description,
            )
        }
    }

    private fun setChecked(
        itemId: Long,
        isChecked: Boolean,
    ) {
        launch {
            setCurrentListItemCheckedUseCase(
                itemId = itemId,
                isChecked = isChecked,
            )
        }
    }

    private fun setAllChecked(isChecked: Boolean) {
        launch {
            setAllCurrentListItemsCheckedUseCase(isChecked)
        }
    }

    private fun removeItem(itemId: Long) {
        launch {
            currentListUndoCoordinator.requestRemoval(itemId)
        }
    }

    private fun reorderItems(itemIdsInOrder: List<Long>) {
        launch {
            reorderCurrentListItemsUseCase(itemIdsInOrder)
        }
    }

    private fun clearList() {
        launch {
            currentListUndoCoordinator.clearList()
        }
    }
}

private fun CurrentListItem.toUiState(): CurrentListItemUiState =
    CurrentListItemUiState(
        id = id,
        title = title,
        description = description,
        isChecked = isChecked,
    )

private const val TestItemCount = 20
