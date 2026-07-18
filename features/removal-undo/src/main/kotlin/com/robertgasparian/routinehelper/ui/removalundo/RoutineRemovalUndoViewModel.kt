package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel
class RoutineRemovalUndoViewModel @Inject constructor(
    private val routineRemovalUndoCoordinator: RoutineRemovalUndoCoordinator,
) : BaseViewModel<RoutineRemovalUndoUiState, RoutineRemovalUndoIntent, Nothing>() {
    override val uiState: StateFlow<RoutineRemovalUndoUiState> =
        routineRemovalUndoCoordinator.state
            .map { state ->
                RoutineRemovalUndoUiState(
                    activeSource = state.activeSource,
                    pendingItemCount = state.pendingItemCount,
                )
            }
            .stateInViewModel(initialValue = RoutineRemovalUndoUiState())

    override fun handleIntent(intent: RoutineRemovalUndoIntent) {
        when (intent) {
            RoutineRemovalUndoIntent.UndoLatestClick -> undoLatest()
            RoutineRemovalUndoIntent.UndoAllClick -> undoAll()
        }
    }

    private fun undoLatest() {
        launch {
            routineRemovalUndoCoordinator.undoLatest()
        }
    }

    private fun undoAll() {
        launch {
            routineRemovalUndoCoordinator.undoAll()
        }
    }
}
