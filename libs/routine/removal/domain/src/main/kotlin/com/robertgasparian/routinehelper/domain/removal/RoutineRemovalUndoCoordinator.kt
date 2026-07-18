package com.robertgasparian.routinehelper.domain.removal

import kotlinx.coroutines.flow.StateFlow

interface RoutineRemovalUndoCoordinator {
    val state: StateFlow<RoutineRemovalUndoState>

    suspend fun finalizeDanglingPendingRemovalsOnLaunch()

    suspend fun requestRemoval(
        source: RoutineRemovalSource,
        itemId: Long,
    ): Boolean

    suspend fun undoLatest()

    suspend fun undoAll()

    suspend fun clearCurrentList(): Boolean
}
