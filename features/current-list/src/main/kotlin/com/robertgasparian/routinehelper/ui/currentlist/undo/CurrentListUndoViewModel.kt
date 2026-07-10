package com.robertgasparian.routinehelper.ui.currentlist.undo

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class CurrentListUndoViewModel @Inject constructor(
    private val currentListUndoCoordinator: CurrentListUndoCoordinator,
) : BaseViewModel<CurrentListUndoUiState, CurrentListUndoIntent, Nothing>() {
    override val uiState: StateFlow<CurrentListUndoUiState> = currentListUndoCoordinator.uiState

    override fun handleIntent(intent: CurrentListUndoIntent) {
        when (intent) {
            CurrentListUndoIntent.UndoLatestClick -> undoLatest()
            CurrentListUndoIntent.UndoAllClick -> undoAll()
        }
    }

    private fun undoLatest() {
        launch {
            currentListUndoCoordinator.undoLatest()
        }
    }

    private fun undoAll() {
        launch {
            currentListUndoCoordinator.undoAll()
        }
    }
}
