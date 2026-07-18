package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.order.RoutineTemplateOrderItem
import com.robertgasparian.routinehelper.domain.order.RoutineTemplateOrderPlanner
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRoutineTemplateRepository @Inject constructor(
    private val database: RoomDatabase,
    private val actionDao: ActionDao,
    private val routineItemDao: RoutineItemDao,
    private val routineTemplateOrderPlanner: RoutineTemplateOrderPlanner,
    private val timeProvider: TimeProvider,
) : RoutineTemplateRepository {
    override fun templateItems(cadence: RoutineCadence): Flow<List<RoutineTemplateItem>> =
        routineItemDao.routineItems(cadence.toStorageValue()).map { items ->
            items.map(RoutineItemWithAction::toDomain)
        }

    override fun templateItem(actionId: Long): Flow<RoutineTemplateItem?> =
        combine(
            templateItems(RoutineCadence.Daily),
            templateItems(RoutineCadence.Weekly),
        ) { dailyItems, weeklyItems ->
            (dailyItems + weeklyItems).firstOrNull { it.actionId == actionId }
        }

    override suspend fun addTemplateItem(
        title: String,
        description: String?,
        repeatTargetCount: Int?,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val now = timeProvider.currentTimeMillis()
        val storageCadence = cadence.toStorageValue()
        val actionId = actionDao.insert(
            ActionEntity(
                title = title.trim(),
                description = description?.trim()?.takeIf(String::isNotEmpty),
                repeatTargetCount = repeatTargetCount,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
        routineItemDao.shiftPositionsForPrepend(cadence = storageCadence)
        routineItemDao.insert(
            RoutineItemEntity(
                actionId = actionId,
                position = 0,
                cadence = storageCadence,
                createdAtMillis = now,
            ),
        )
    }

    override suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
        repeatTargetCount: Int?,
    ) {
        val existing = actionDao.action(actionId).first() ?: return
        actionDao.update(
            existing.copy(
                title = title.trim(),
                description = description?.trim()?.takeIf(String::isNotEmpty),
                repeatTargetCount = repeatTargetCount,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
        )
    }

    override suspend fun removeTemplateItem(routineItemId: Long) {
        database.withTransaction {
            val routineItem = routineItemDao.routineItem(routineItemId).first() ?: return@withTransaction
            routineItemDao.deleteById(routineItemId)
            actionDao.deleteById(routineItem.actionId)
        }
    }

    override suspend fun markPendingRemoval(
        cadence: RoutineCadence,
        routineItemId: Long,
    ) {
        routineItemDao.markPendingRemoval(
            cadence = cadence.toStorageValue(),
            routineItemId = routineItemId,
            pendingRemovalAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun restorePendingRemoval(
        cadence: RoutineCadence,
        routineItemId: Long,
    ) {
        routineItemDao.restorePendingRemoval(
            cadence = cadence.toStorageValue(),
            routineItemId = routineItemId,
        )
    }

    override suspend fun restorePendingRemovals(
        cadence: RoutineCadence,
        routineItemIds: List<Long>,
    ) {
        if (routineItemIds.isEmpty()) return
        routineItemDao.restorePendingRemovals(
            cadence = cadence.toStorageValue(),
            routineItemIds = routineItemIds,
        )
    }

    override suspend fun deletePendingRemovals(
        cadence: RoutineCadence,
        routineItemIds: List<Long>,
    ) {
        if (routineItemIds.isEmpty()) return
        val storageCadence = cadence.toStorageValue()
        database.withTransaction {
            val pendingItems = routineItemDao.pendingRemovalsSnapshot(
                cadence = storageCadence,
                routineItemIds = routineItemIds,
            )
            routineItemDao.deletePendingRemovals(
                cadence = storageCadence,
                routineItemIds = routineItemIds,
            )
            pendingItems.forEach { item -> actionDao.deleteById(item.actionId) }
        }
    }

    override suspend fun deleteAllPendingRemovals() {
        database.withTransaction {
            val pendingItems = routineItemDao.allPendingRemovalsSnapshot()
            routineItemDao.deleteAllPendingRemovals()
            pendingItems.forEach { item -> actionDao.deleteById(item.actionId) }
        }
    }

    override suspend fun reorderTemplateItems(
        cadence: RoutineCadence,
        routineItemIdsInOrder: List<Long>,
    ) {
        val storageCadence = cadence.toStorageValue()
        database.withTransaction {
            val allItems = routineItemDao.allRoutineItemsSnapshot(storageCadence)
            val positionUpdates = routineTemplateOrderPlanner.planReorder(
                allItems = allItems.map(RoutineItemEntity::toOrderItem),
                visibleItemIdsInOrder = routineItemIdsInOrder,
            )
            positionUpdates.forEach { update ->
                routineItemDao.updatePosition(
                    cadence = storageCadence,
                    routineItemId = update.itemId,
                    position = update.position,
                )
            }
        }
    }
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE
        RoutineCadence.Weekly -> RoutineItemEntity.WEEKLY_CADENCE_STORAGE_VALUE
    }

private fun String.toRoutineCadence(): RoutineCadence =
    when (this) {
        RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE -> RoutineCadence.Daily
        RoutineItemEntity.WEEKLY_CADENCE_STORAGE_VALUE -> RoutineCadence.Weekly
        else -> error("Unsupported routine cadence storage value: $this")
    }

private fun RoutineItemWithAction.toDomain(): RoutineTemplateItem =
    RoutineTemplateItem(
        routineItemId = routineItem.id,
        actionId = action.id,
        title = action.title,
        description = action.description,
        repeatTargetCount = action.repeatTargetCount,
        position = routineItem.position,
        cadence = routineItem.cadence.toRoutineCadence(),
    )

private fun RoutineItemEntity.toOrderItem(): RoutineTemplateOrderItem =
    RoutineTemplateOrderItem(
        id = id,
        position = position,
        isPendingRemoval = pendingRemovalAtMillis != null,
    )
