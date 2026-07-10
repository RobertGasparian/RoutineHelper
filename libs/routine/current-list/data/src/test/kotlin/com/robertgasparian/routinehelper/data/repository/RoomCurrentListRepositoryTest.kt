package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.data.local.dao.CurrentListItemDao
import com.robertgasparian.routinehelper.data.local.entity.CurrentListItemEntity
import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import com.robertgasparian.routinehelper.domain.order.CurrentListOrderPlanner
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomCurrentListRepositoryTest {
    private val database = CurrentListTestRoomDatabase()
    private val currentListItemDao = FakeCurrentListItemDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomCurrentListRepository(
        database = database,
        currentListItemDao = currentListItemDao,
        currentListOrderPlanner = CurrentListOrderPlanner(),
        timeProvider = timeProvider,
    )

    @Test
    fun `given stored items when observing current list then maps them to domain`() = runTest {
        currentListItemDao.items += currentListItemEntity(
            id = 10L,
            title = "Pick up dry cleaning",
            description = "Before 6 PM",
            position = 0,
            isChecked = true,
        )
        currentListItemDao.items += currentListItemEntity(
            id = 11L,
            title = "Pending removal",
            position = 1,
            pendingRemovalAtMillis = 100L,
        )

        val items = repository.currentListItems().first()

        assertEquals(
            listOf(
                CurrentListItem(
                    id = 10L,
                    title = "Pick up dry cleaning",
                    description = "Before 6 PM",
                    position = 0,
                    isChecked = true,
                ),
            ),
            items,
        )
    }

    @Test
    fun `given existing items when adding item then appends normalized unchecked item`() = runTest {
        currentListItemDao.items += currentListItemEntity(id = 10L, position = 2)

        val itemId = repository.addItem(
            title = "  Order filters  ",
            description = "  HVAC  ",
        )

        assertEquals(11L, itemId)
        assertEquals(
            currentListItemEntity(
                id = 11L,
                title = "Order filters",
                description = "HVAC",
                position = 3,
                isChecked = false,
                createdAtMillis = timeProvider.currentTimeMillis(),
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            currentListItemDao.items.last(),
        )
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `when mutating item state then forwards checked pending all checked and clear operations`() = runTest {
        repository.setChecked(itemId = 10L, isChecked = true)
        repository.setAllChecked(isChecked = false)
        repository.markPendingRemoval(itemId = 12L)
        repository.restorePendingRemoval(itemId = 13L)
        repository.restorePendingRemovals(listOf(14L, 15L))
        repository.deletePendingRemovals(listOf(16L, 17L))
        repository.deleteAllPendingRemovals()
        repository.clearItems()

        assertEquals(
            listOf(CheckedUpdate(10L, true, timeProvider.currentTimeMillis())),
            currentListItemDao.checkedUpdates,
        )
        assertEquals(
            listOf(AllCheckedUpdate(false, timeProvider.currentTimeMillis())),
            currentListItemDao.allCheckedUpdates,
        )
        assertEquals(
            listOf(PendingRemovalUpdate(12L, timeProvider.currentTimeMillis())),
            currentListItemDao.pendingRemovalUpdates,
        )
        assertEquals(
            listOf(PendingRemovalRestore(13L, timeProvider.currentTimeMillis())),
            currentListItemDao.pendingRemovalRestores,
        )
        assertEquals(
            listOf(PendingRemovalGroupRestore(listOf(14L, 15L), timeProvider.currentTimeMillis())),
            currentListItemDao.pendingRemovalGroupRestores,
        )
        assertEquals(listOf(listOf(16L, 17L)), currentListItemDao.deletedPendingRemovalItemIds)
        assertEquals(1, currentListItemDao.deleteAllPendingRemovalsCount)
        assertEquals(1, currentListItemDao.deleteAllCount)
    }

    @Test
    fun `given no pending ids when restoring or deleting pending removals then skips dao calls`() = runTest {
        repository.restorePendingRemovals(emptyList())
        repository.deletePendingRemovals(emptyList())

        assertEquals(emptyList<PendingRemovalGroupRestore>(), currentListItemDao.pendingRemovalGroupRestores)
        assertEquals(emptyList<List<Long>>(), currentListItemDao.deletedPendingRemovalItemIds)
    }

    @Test
    fun `given stored items when reordering then updates positions for known ids`() = runTest {
        currentListItemDao.items += currentListItemEntity(id = 10L, position = 0)
        currentListItemDao.items += currentListItemEntity(id = 11L, position = 1)

        repository.reorderItems(listOf(11L, 10L, 999L))

        assertEquals(
            listOf(
                PositionUpdate(
                    itemId = 11L,
                    position = 0,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
                PositionUpdate(
                    itemId = 10L,
                    position = 1,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
            ),
            currentListItemDao.positionUpdates,
        )
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `given pending removal when reordering then preserves hidden item slot`() = runTest {
        currentListItemDao.items += currentListItemEntity(id = 10L, position = 0)
        currentListItemDao.items += currentListItemEntity(
            id = 11L,
            position = 1,
            pendingRemovalAtMillis = 100L,
        )
        currentListItemDao.items += currentListItemEntity(id = 12L, position = 2)
        currentListItemDao.items += currentListItemEntity(id = 13L, position = 3)

        repository.reorderItems(listOf(13L, 10L, 12L))

        assertEquals(
            listOf(
                PositionUpdate(
                    itemId = 13L,
                    position = 0,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
                PositionUpdate(
                    itemId = 10L,
                    position = 2,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
                PositionUpdate(
                    itemId = 12L,
                    position = 3,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
            ),
            currentListItemDao.positionUpdates,
        )
        assertEquals(
            listOf(13L, 11L, 10L, 12L),
            currentListItemDao.items
                .sortedWith(compareBy(CurrentListItemEntity::position, CurrentListItemEntity::id))
                .map(CurrentListItemEntity::id),
        )
        assertEquals(1, database.transactionSuccesses)
    }

    private fun currentListItemEntity(
        id: Long,
        title: String = "Task",
        description: String? = null,
        position: Int = 0,
        isChecked: Boolean = false,
        pendingRemovalAtMillis: Long? = null,
        createdAtMillis: Long = 1L,
        updatedAtMillis: Long = 1L,
    ): CurrentListItemEntity =
        CurrentListItemEntity(
            id = id,
            title = title,
            description = description,
            position = position,
            isChecked = isChecked,
            pendingRemovalAtMillis = pendingRemovalAtMillis,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
        )
}

private class FakeCurrentListItemDao : CurrentListItemDao {
    val items = mutableListOf<CurrentListItemEntity>()
    val checkedUpdates = mutableListOf<CheckedUpdate>()
    val allCheckedUpdates = mutableListOf<AllCheckedUpdate>()
    val pendingRemovalUpdates = mutableListOf<PendingRemovalUpdate>()
    val pendingRemovalRestores = mutableListOf<PendingRemovalRestore>()
    val pendingRemovalGroupRestores = mutableListOf<PendingRemovalGroupRestore>()
    val deletedPendingRemovalItemIds = mutableListOf<List<Long>>()
    val positionUpdates = mutableListOf<PositionUpdate>()
    var deleteAllCount = 0
    var deleteAllPendingRemovalsCount = 0

    override fun currentListItems(): Flow<List<CurrentListItemEntity>> =
        flowOf(
            items
                .filter { item -> item.pendingRemovalAtMillis == null }
                .sortedWith(compareBy(CurrentListItemEntity::position, CurrentListItemEntity::id)),
        )

    override suspend fun allCurrentListItemsSnapshot(): List<CurrentListItemEntity> =
        items.sortedWith(compareBy(CurrentListItemEntity::position, CurrentListItemEntity::id))

    override suspend fun maxPosition(): Int = items.maxOfOrNull(CurrentListItemEntity::position) ?: -1

    override suspend fun insert(item: CurrentListItemEntity): Long {
        val nextId = items.maxOfOrNull(CurrentListItemEntity::id)?.plus(1L) ?: 1L
        val insertedItem = item.copy(id = nextId)
        items += insertedItem
        return insertedItem.id
    }

    override suspend fun updatePosition(
        itemId: Long,
        position: Int,
        updatedAtMillis: Long,
    ) {
        positionUpdates += PositionUpdate(itemId, position, updatedAtMillis)
        val index = items.indexOfFirst { existingItem -> existingItem.id == itemId }
        if (index >= 0) {
            items[index] = items[index].copy(
                position = position,
                updatedAtMillis = updatedAtMillis,
            )
        }
    }

    override suspend fun updateChecked(
        itemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    ) {
        checkedUpdates += CheckedUpdate(itemId, isChecked, updatedAtMillis)
    }

    override suspend fun updateAllChecked(
        isChecked: Boolean,
        updatedAtMillis: Long,
    ) {
        allCheckedUpdates += AllCheckedUpdate(isChecked, updatedAtMillis)
    }

    override suspend fun markPendingRemoval(
        itemId: Long,
        pendingRemovalAtMillis: Long,
    ) {
        pendingRemovalUpdates += PendingRemovalUpdate(itemId, pendingRemovalAtMillis)
    }

    override suspend fun restorePendingRemoval(
        itemId: Long,
        updatedAtMillis: Long,
    ) {
        pendingRemovalRestores += PendingRemovalRestore(itemId, updatedAtMillis)
    }

    override suspend fun restorePendingRemovals(
        itemIds: List<Long>,
        updatedAtMillis: Long,
    ) {
        pendingRemovalGroupRestores += PendingRemovalGroupRestore(itemIds, updatedAtMillis)
    }

    override suspend fun deletePendingRemovals(itemIds: List<Long>) {
        deletedPendingRemovalItemIds += itemIds
    }

    override suspend fun deleteAllPendingRemovals() {
        deleteAllPendingRemovalsCount += 1
    }

    override suspend fun deleteAll() {
        deleteAllCount += 1
    }
}

private data class CheckedUpdate(
    val itemId: Long,
    val isChecked: Boolean,
    val updatedAtMillis: Long,
)

private data class AllCheckedUpdate(
    val isChecked: Boolean,
    val updatedAtMillis: Long,
)

private data class PositionUpdate(
    val itemId: Long,
    val position: Int,
    val updatedAtMillis: Long,
)

private data class PendingRemovalUpdate(
    val itemId: Long,
    val pendingRemovalAtMillis: Long,
)

private data class PendingRemovalRestore(
    val itemId: Long,
    val updatedAtMillis: Long,
)

private data class PendingRemovalGroupRestore(
    val itemIds: List<Long>,
    val updatedAtMillis: Long,
)

@Suppress("OVERRIDE_DEPRECATION")
private class CurrentListTestRoomDatabase : RoomDatabase() {
    var transactionSuccesses = 0
        private set

    override val transactionExecutor: Executor = Executor(Runnable::run)

    override fun createInvalidationTracker(): InvalidationTracker =
        error("Invalidation tracking is not used by repository unit tests")

    override fun clearAllTables() = Unit

    override fun beginTransaction() = Unit

    override fun setTransactionSuccessful() {
        transactionSuccesses += 1
    }

    override fun endTransaction() = Unit
}
