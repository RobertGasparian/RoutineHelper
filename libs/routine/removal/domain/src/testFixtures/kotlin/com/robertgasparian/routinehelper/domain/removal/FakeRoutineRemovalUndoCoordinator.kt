package com.robertgasparian.routinehelper.domain.removal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRoutineRemovalUndoCoordinator(
    initialState: RoutineRemovalUndoState = RoutineRemovalUndoState(),
) : RoutineRemovalUndoCoordinator {
    private val mutableState = MutableStateFlow(initialState)

    override val state: StateFlow<RoutineRemovalUndoState> = mutableState.asStateFlow()

    val removalRequests = mutableListOf<RoutineRemovalRequest>()
    var finalizeDanglingPendingRemovalsCount = 0
    var undoLatestCount = 0
    var undoAllCount = 0
    var clearCurrentListCount = 0

    fun setState(state: RoutineRemovalUndoState) {
        mutableState.value = state
    }

    override suspend fun finalizeDanglingPendingRemovalsOnLaunch() {
        finalizeDanglingPendingRemovalsCount += 1
    }

    override suspend fun requestRemoval(
        source: RoutineRemovalSource,
        itemId: Long,
    ): Boolean {
        if (!state.value.allowsRemovalFrom(source)) return false
        removalRequests += RoutineRemovalRequest(source, itemId)
        mutableState.value = RoutineRemovalUndoState(
            activeSource = source,
            pendingItemCount = state.value.pendingItemCount + 1,
        )
        return true
    }

    override suspend fun undoLatest() {
        undoLatestCount += 1
    }

    override suspend fun undoAll() {
        undoAllCount += 1
    }

    override suspend fun clearCurrentList(): Boolean {
        if (!state.value.allowsRemovalFrom(RoutineRemovalSource.CurrentList)) return false
        clearCurrentListCount += 1
        mutableState.value = RoutineRemovalUndoState()
        return true
    }
}

data class RoutineRemovalRequest(
    val source: RoutineRemovalSource,
    val itemId: Long,
)
