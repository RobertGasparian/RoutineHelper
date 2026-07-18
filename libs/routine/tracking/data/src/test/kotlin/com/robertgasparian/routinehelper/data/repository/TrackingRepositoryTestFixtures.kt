package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeRoutineItemDao : RoutineItemDao {
    var items: List<RoutineItemWithAction> = emptyList()
    var requestedCadence: String? = null

    override fun routineItems(cadence: String): Flow<List<RoutineItemWithAction>> {
        requestedCadence = cadence
        return flowOf(items)
    }

    override suspend fun allRoutineItemsSnapshot(cadence: String): List<RoutineItemEntity> = emptyList()

    override fun maxPosition(cadence: String): Flow<Int> = flowOf(-1)

    override fun routineItem(id: Long): Flow<RoutineItemEntity?> = flowOf(null)

    override suspend fun insert(routineItem: RoutineItemEntity): Long = routineItem.id

    override suspend fun update(routineItem: RoutineItemEntity) = Unit

    override suspend fun markPendingRemoval(
        cadence: String,
        routineItemId: Long,
        pendingRemovalAtMillis: Long,
    ) = Unit

    override suspend fun restorePendingRemoval(
        cadence: String,
        routineItemId: Long,
    ) = Unit

    override suspend fun restorePendingRemovals(
        cadence: String,
        routineItemIds: List<Long>,
    ) = Unit

    override suspend fun pendingRemovalsSnapshot(
        cadence: String,
        routineItemIds: List<Long>,
    ): List<RoutineItemEntity> = emptyList()

    override suspend fun allPendingRemovalsSnapshot(): List<RoutineItemEntity> = emptyList()

    override suspend fun deletePendingRemovals(
        cadence: String,
        routineItemIds: List<Long>,
    ) = Unit

    override suspend fun deleteAllPendingRemovals() = Unit

    override suspend fun updatePosition(
        cadence: String,
        routineItemId: Long,
        position: Int,
    ) = Unit

    override suspend fun deleteById(id: Long) = Unit
}

internal fun routineItemFixture(
    routineItemId: Long,
    actionId: Long,
    title: String,
    position: Int,
    repeatTargetCount: Int? = null,
    cadence: String = "DAILY",
): RoutineItemWithAction =
    RoutineItemWithAction(
        routineItem = RoutineItemEntity(
            id = routineItemId,
            actionId = actionId,
            position = position,
            cadence = cadence,
            createdAtMillis = 1L,
        ),
        action = ActionEntity(
            id = actionId,
            title = title,
            repeatTargetCount = repeatTargetCount,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        ),
    )

@Suppress("OVERRIDE_DEPRECATION")
internal class TrackingTestRoomDatabase : RoomDatabase() {
    var transactionBegins = 0
        private set
    var transactionSuccesses = 0
        private set
    var transactionEnds = 0
        private set

    override val transactionExecutor: Executor = Executor(Runnable::run)

    override fun createInvalidationTracker(): InvalidationTracker =
        error("Invalidation tracking is not used by repository unit tests")

    override fun clearAllTables() = Unit

    override fun beginTransaction() {
        transactionBegins += 1
    }

    override fun setTransactionSuccessful() {
        transactionSuccesses += 1
    }

    override fun endTransaction() {
        transactionEnds += 1
    }
}
