package com.robertgasparian.routinehelper.domain.removal

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
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
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultRoutineRemovalUndoCoordinator @Inject constructor(
    private val markCurrentListItemPendingRemovalUseCase: MarkCurrentListItemPendingRemovalUseCase,
    private val restoreCurrentListItemPendingRemovalUseCase: RestoreCurrentListItemPendingRemovalUseCase,
    private val restoreCurrentListItemsPendingRemovalUseCase: RestoreCurrentListItemsPendingRemovalUseCase,
    private val deleteCurrentListPendingRemovalsUseCase: DeleteCurrentListPendingRemovalsUseCase,
    private val deleteAllCurrentListPendingRemovalsUseCase: DeleteAllCurrentListPendingRemovalsUseCase,
    private val clearCurrentListUseCase: ClearCurrentListUseCase,
    private val markTemplateItemPendingRemovalUseCase: MarkTemplateItemPendingRemovalUseCase,
    private val restoreTemplateItemPendingRemovalUseCase: RestoreTemplateItemPendingRemovalUseCase,
    private val restoreTemplateItemsPendingRemovalUseCase: RestoreTemplateItemsPendingRemovalUseCase,
    private val deleteTemplatePendingRemovalsUseCase: DeleteTemplatePendingRemovalsUseCase,
    private val deleteAllTemplatePendingRemovalsUseCase: DeleteAllTemplatePendingRemovalsUseCase,
    @param:RoutineRemovalUndoScope private val coroutineScope: CoroutineScope,
) : RoutineRemovalUndoCoordinator {
    private val mutex = Mutex()
    private val pendingItemIds = mutableListOf<Long>()
    private val mutableState = MutableStateFlow(RoutineRemovalUndoState())
    private var activeSource: RoutineRemovalSource? = null
    private var timerJob: Job? = null

    override val state: StateFlow<RoutineRemovalUndoState> = mutableState.asStateFlow()

    override suspend fun finalizeDanglingPendingRemovalsOnLaunch() {
        mutex.withLock {
            if (pendingItemIds.isEmpty()) {
                deleteAllCurrentListPendingRemovalsUseCase()
                deleteAllTemplatePendingRemovalsUseCase()
            }
        }
    }

    override suspend fun requestRemoval(
        source: RoutineRemovalSource,
        itemId: Long,
    ): Boolean = mutex.withLock {
        if (activeSource != null && activeSource != source) {
            return@withLock false
        }

        markPendingRemoval(source = source, itemId = itemId)
        activeSource = source
        pendingItemIds.remove(itemId)
        pendingItemIds += itemId
        publishLocked()
        resetTimerLocked()
        true
    }

    override suspend fun undoLatest() {
        mutex.withLock {
            val source = activeSource ?: return
            val itemId = pendingItemIds.lastOrNull() ?: return
            restorePendingRemoval(source = source, itemId = itemId)
            pendingItemIds.remove(itemId)
            if (pendingItemIds.isEmpty()) {
                activeSource = null
                clearTimerLocked()
            } else {
                resetTimerLocked()
            }
            publishLocked()
        }
    }

    override suspend fun undoAll() {
        mutex.withLock {
            val source = activeSource ?: return
            if (pendingItemIds.isEmpty()) return
            restorePendingRemovals(source = source, itemIds = pendingItemIds.toList())
            pendingItemIds.clear()
            activeSource = null
            clearTimerLocked()
            publishLocked()
        }
    }

    override suspend fun clearCurrentList(): Boolean = mutex.withLock {
        if (activeSource != null && activeSource != RoutineRemovalSource.CurrentList) {
            return@withLock false
        }

        clearCurrentListUseCase()
        pendingItemIds.clear()
        activeSource = null
        clearTimerLocked()
        publishLocked()
        true
    }

    private suspend fun finalizePendingRemovalsFromTimer() {
        mutex.withLock {
            val source = activeSource ?: return
            deletePendingRemovals(source = source, itemIds = pendingItemIds.toList())
            timerJob = null
            pendingItemIds.clear()
            activeSource = null
            publishLocked()
        }
    }

    private suspend fun markPendingRemoval(
        source: RoutineRemovalSource,
        itemId: Long,
    ) {
        when (source) {
            RoutineRemovalSource.CurrentList -> markCurrentListItemPendingRemovalUseCase(itemId)
            RoutineRemovalSource.Daily,
            RoutineRemovalSource.Weekly,
            -> markTemplateItemPendingRemovalUseCase(
                cadence = source.toCadence(),
                routineItemId = itemId,
            )
        }
    }

    private suspend fun restorePendingRemoval(
        source: RoutineRemovalSource,
        itemId: Long,
    ) {
        when (source) {
            RoutineRemovalSource.CurrentList -> restoreCurrentListItemPendingRemovalUseCase(itemId)
            RoutineRemovalSource.Daily,
            RoutineRemovalSource.Weekly,
            -> restoreTemplateItemPendingRemovalUseCase(
                cadence = source.toCadence(),
                routineItemId = itemId,
            )
        }
    }

    private suspend fun restorePendingRemovals(
        source: RoutineRemovalSource,
        itemIds: List<Long>,
    ) {
        when (source) {
            RoutineRemovalSource.CurrentList -> restoreCurrentListItemsPendingRemovalUseCase(itemIds)
            RoutineRemovalSource.Daily,
            RoutineRemovalSource.Weekly,
            -> restoreTemplateItemsPendingRemovalUseCase(
                cadence = source.toCadence(),
                routineItemIds = itemIds,
            )
        }
    }

    private suspend fun deletePendingRemovals(
        source: RoutineRemovalSource,
        itemIds: List<Long>,
    ) {
        when (source) {
            RoutineRemovalSource.CurrentList -> deleteCurrentListPendingRemovalsUseCase(itemIds)
            RoutineRemovalSource.Daily,
            RoutineRemovalSource.Weekly,
            -> deleteTemplatePendingRemovalsUseCase(
                cadence = source.toCadence(),
                routineItemIds = itemIds,
            )
        }
    }

    private fun resetTimerLocked() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            delay(RoutineRemovalUndoDurationMillis)
            finalizePendingRemovalsFromTimer()
        }
    }

    private fun clearTimerLocked() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun publishLocked() {
        mutableState.value = RoutineRemovalUndoState(
            activeSource = activeSource,
            pendingItemCount = pendingItemIds.size,
        )
    }
}

private fun RoutineRemovalSource.toCadence(): RoutineCadence =
    when (this) {
        RoutineRemovalSource.Daily -> RoutineCadence.Daily
        RoutineRemovalSource.Weekly -> RoutineCadence.Weekly
        RoutineRemovalSource.CurrentList -> error("Current List removals do not have a cadence")
    }

private const val RoutineRemovalUndoDurationMillis = 4_000L
