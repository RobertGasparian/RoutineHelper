package com.robertgasparian.routinehelper.ui.currentlist.undo

import com.robertgasparian.routinehelper.domain.usecase.ClearCurrentListUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteAllCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.DeleteCurrentListPendingRemovalsUseCase
import com.robertgasparian.routinehelper.domain.usecase.MarkCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemPendingRemovalUseCase
import com.robertgasparian.routinehelper.domain.usecase.RestoreCurrentListItemsPendingRemovalUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class CurrentListUndoCoordinator @Inject constructor(
    private val markCurrentListItemPendingRemovalUseCase: MarkCurrentListItemPendingRemovalUseCase,
    private val restoreCurrentListItemPendingRemovalUseCase: RestoreCurrentListItemPendingRemovalUseCase,
    private val restoreCurrentListItemsPendingRemovalUseCase: RestoreCurrentListItemsPendingRemovalUseCase,
    private val deleteCurrentListPendingRemovalsUseCase: DeleteCurrentListPendingRemovalsUseCase,
    private val deleteAllCurrentListPendingRemovalsUseCase: DeleteAllCurrentListPendingRemovalsUseCase,
    private val clearCurrentListUseCase: ClearCurrentListUseCase,
    @param:CurrentListUndoScope private val coroutineScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val pendingItemIds = mutableListOf<Long>()
    private val mutableUiState = MutableStateFlow(CurrentListUndoUiState())
    private var timerJob: Job? = null

    val uiState: StateFlow<CurrentListUndoUiState> = mutableUiState.asStateFlow()

    suspend fun finalizeDanglingPendingRemovalsOnLaunch() {
        mutex.withLock {
            if (pendingItemIds.isEmpty()) {
                deleteAllCurrentListPendingRemovalsUseCase()
            }
        }
    }

    suspend fun requestRemoval(itemId: Long) {
        mutex.withLock {
            markCurrentListItemPendingRemovalUseCase(itemId)
            pendingItemIds.remove(itemId)
            pendingItemIds += itemId
            publishLocked()
            resetTimerLocked()
        }
    }

    suspend fun undoLatest() {
        mutex.withLock {
            val itemId = pendingItemIds.lastOrNull() ?: return
            restoreCurrentListItemPendingRemovalUseCase(itemId)
            pendingItemIds.remove(itemId)
            publishLocked()
            if (pendingItemIds.isEmpty()) {
                clearTimerLocked()
            } else {
                resetTimerLocked()
            }
        }
    }

    suspend fun undoAll() {
        mutex.withLock {
            if (pendingItemIds.isEmpty()) return
            restoreCurrentListItemsPendingRemovalUseCase(pendingItemIds.toList())
            pendingItemIds.clear()
            publishLocked()
            clearTimerLocked()
        }
    }

    suspend fun clearList() {
        mutex.withLock {
            clearCurrentListUseCase()
            pendingItemIds.clear()
            clearTimerLocked()
            publishLocked()
        }
    }

    private suspend fun finalizePendingRemovalsFromTimer() {
        mutex.withLock {
            val itemIds = pendingItemIds.toList()
            deleteCurrentListPendingRemovalsUseCase(itemIds)
            timerJob = null
            pendingItemIds.clear()
            publishLocked()
        }
    }

    private fun resetTimerLocked() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            delay(CurrentListUndoDurationMillis)
            finalizePendingRemovalsFromTimer()
        }
    }

    private fun clearTimerLocked() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun publishLocked() {
        mutableUiState.value = CurrentListUndoUiState(
            pendingItemCount = pendingItemIds.size,
        )
    }
}

private const val CurrentListUndoDurationMillis = 4_000L
