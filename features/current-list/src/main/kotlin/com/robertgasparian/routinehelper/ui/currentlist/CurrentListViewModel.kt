package com.robertgasparian.routinehelper.ui.currentlist

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.usecase.AddCurrentListItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.CurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderCurrentListItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetAllCurrentListItemsCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetCurrentListItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateCurrentListItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class CurrentListViewModel @Inject constructor(
    currentListItemsUseCase: CurrentListItemsUseCase,
    private val addCurrentListItemUseCase: AddCurrentListItemUseCase,
    private val updateCurrentListItemUseCase: UpdateCurrentListItemUseCase,
    private val reorderCurrentListItemsUseCase: ReorderCurrentListItemsUseCase,
    private val setAllCurrentListItemsCheckedUseCase: SetAllCurrentListItemsCheckedUseCase,
    private val setCurrentListItemCheckedUseCase: SetCurrentListItemCheckedUseCase,
    private val currentListTextProvider: CurrentListTextProvider,
    private val routineRemovalUndoCoordinator: RoutineRemovalUndoCoordinator,
) : BaseViewModel<CurrentListUiState, CurrentListIntent, Nothing>() {
    override val uiState: StateFlow<CurrentListUiState> =
        combine(
            currentListItemsUseCase(),
            routineRemovalUndoCoordinator.state,
        ) { items, removalState ->
            CurrentListUiState(
                items = items.map(CurrentListItem::toUiState),
                shareText = currentListTextProvider.shareText(items),
                canRemoveItems = removalState.allowsRemovalFrom(RoutineRemovalSource.CurrentList),
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
            is CurrentListIntent.UpdateItem -> updateItem(
                itemId = intent.itemId,
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
        val firstItemNumber = uiState.value.items.size + 1
        launch {
            repeat(DebugItemBatchSize) { offset ->
                val itemNumber = firstItemNumber + offset
                addCurrentListItemUseCase(
                    title = currentListTextProvider.debugItemTitle(itemNumber),
                    description = if (itemNumber % 2 == 0) {
                        currentListTextProvider.debugItemDescription(itemNumber)
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

    private fun updateItem(
        itemId: Long,
        title: String,
        description: String?,
    ) {
        launch {
            updateCurrentListItemUseCase(
                itemId = itemId,
                title = title,
                description = description,
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
            routineRemovalUndoCoordinator.requestRemoval(
                source = RoutineRemovalSource.CurrentList,
                itemId = itemId,
            )
        }
    }

    private fun reorderItems(itemIdsInOrder: List<Long>) {
        launch {
            reorderCurrentListItemsUseCase(itemIdsInOrder)
        }
    }

    private fun clearList() {
        launch {
            routineRemovalUndoCoordinator.clearCurrentList()
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

private const val DebugItemBatchSize = 20
