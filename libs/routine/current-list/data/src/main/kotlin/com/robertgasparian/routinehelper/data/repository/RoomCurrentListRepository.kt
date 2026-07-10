package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.CurrentListItemDao
import com.robertgasparian.routinehelper.data.local.entity.CurrentListItemEntity
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.order.CurrentListOrderItem
import com.robertgasparian.routinehelper.domain.order.CurrentListOrderPlanner
import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCurrentListRepository @Inject constructor(
    private val database: RoomDatabase,
    private val currentListItemDao: CurrentListItemDao,
    private val currentListOrderPlanner: CurrentListOrderPlanner,
    private val timeProvider: TimeProvider,
) : CurrentListRepository {
    override fun currentListItems(): Flow<List<CurrentListItem>> =
        currentListItemDao.currentListItems().map { items ->
            items.map(CurrentListItemEntity::toDomain)
        }

    override suspend fun addItem(
        title: String,
        description: String?,
    ): Long = database.withTransaction {
        val now = timeProvider.currentTimeMillis()
        currentListItemDao.insert(
            CurrentListItemEntity(
                title = title.trim(),
                description = description?.trim()?.takeIf(String::isNotEmpty),
                position = currentListItemDao.maxPosition() + 1,
                isChecked = false,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
    }

    override suspend fun setChecked(
        itemId: Long,
        isChecked: Boolean,
    ) {
        currentListItemDao.updateChecked(
            itemId = itemId,
            isChecked = isChecked,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun setAllChecked(isChecked: Boolean) {
        currentListItemDao.updateAllChecked(
            isChecked = isChecked,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun markPendingRemoval(itemId: Long) {
        currentListItemDao.markPendingRemoval(
            itemId = itemId,
            pendingRemovalAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun restorePendingRemoval(itemId: Long) {
        currentListItemDao.restorePendingRemoval(
            itemId = itemId,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun restorePendingRemovals(itemIds: List<Long>) {
        if (itemIds.isEmpty()) return
        currentListItemDao.restorePendingRemovals(
            itemIds = itemIds,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun deletePendingRemovals(itemIds: List<Long>) {
        if (itemIds.isEmpty()) return
        currentListItemDao.deletePendingRemovals(itemIds)
    }

    override suspend fun deleteAllPendingRemovals() {
        currentListItemDao.deleteAllPendingRemovals()
    }

    override suspend fun reorderItems(itemIdsInOrder: List<Long>) {
        database.withTransaction {
            val allItems = currentListItemDao.allCurrentListItemsSnapshot()
            val positionUpdates = currentListOrderPlanner.planReorder(
                allItems = allItems.map(CurrentListItemEntity::toOrderItem),
                visibleItemIdsInOrder = itemIdsInOrder,
            )
            val updatedAtMillis = timeProvider.currentTimeMillis()
            positionUpdates.forEach { update ->
                currentListItemDao.updatePosition(
                    itemId = update.itemId,
                    position = update.position,
                    updatedAtMillis = updatedAtMillis,
                )
            }
        }
    }

    override suspend fun clearItems() {
        currentListItemDao.deleteAll()
    }
}

private fun CurrentListItemEntity.toDomain(): CurrentListItem =
    CurrentListItem(
        id = id,
        title = title,
        description = description,
        position = position,
        isChecked = isChecked,
    )

private fun CurrentListItemEntity.toOrderItem(): CurrentListOrderItem =
    CurrentListOrderItem(
        id = id,
        position = position,
        isPendingRemoval = pendingRemovalAtMillis != null,
    )
