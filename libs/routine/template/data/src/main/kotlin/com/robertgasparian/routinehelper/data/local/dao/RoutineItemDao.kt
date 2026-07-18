package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineItemDao {
    @Transaction
    @Query(
        "SELECT * FROM routine_items " +
            "WHERE cadence = :cadence AND pendingRemovalAtMillis IS NULL " +
            "ORDER BY position",
    )
    fun routineItems(cadence: String): Flow<List<RoutineItemWithAction>>

    @Query("SELECT * FROM routine_items WHERE cadence = :cadence ORDER BY position, id")
    suspend fun allRoutineItemsSnapshot(cadence: String): List<RoutineItemEntity>

    @Query("UPDATE routine_items SET position = position + 1 WHERE cadence = :cadence")
    suspend fun shiftPositionsForPrepend(cadence: String)

    @Query("SELECT * FROM routine_items WHERE id = :id")
    fun routineItem(id: Long): Flow<RoutineItemEntity?>

    @Insert
    suspend fun insert(routineItem: RoutineItemEntity): Long

    @Update
    suspend fun update(routineItem: RoutineItemEntity)

    @Query(
        "UPDATE routine_items SET pendingRemovalAtMillis = :pendingRemovalAtMillis " +
            "WHERE id = :routineItemId AND cadence = :cadence",
    )
    suspend fun markPendingRemoval(
        cadence: String,
        routineItemId: Long,
        pendingRemovalAtMillis: Long,
    )

    @Query(
        "UPDATE routine_items SET pendingRemovalAtMillis = NULL " +
            "WHERE id = :routineItemId AND cadence = :cadence",
    )
    suspend fun restorePendingRemoval(
        cadence: String,
        routineItemId: Long,
    )

    @Query(
        "UPDATE routine_items SET pendingRemovalAtMillis = NULL " +
            "WHERE id IN (:routineItemIds) AND cadence = :cadence",
    )
    suspend fun restorePendingRemovals(
        cadence: String,
        routineItemIds: List<Long>,
    )

    @Query(
        "SELECT * FROM routine_items " +
            "WHERE id IN (:routineItemIds) AND cadence = :cadence " +
            "AND pendingRemovalAtMillis IS NOT NULL",
    )
    suspend fun pendingRemovalsSnapshot(
        cadence: String,
        routineItemIds: List<Long>,
    ): List<RoutineItemEntity>

    @Query("SELECT * FROM routine_items WHERE pendingRemovalAtMillis IS NOT NULL")
    suspend fun allPendingRemovalsSnapshot(): List<RoutineItemEntity>

    @Query(
        "DELETE FROM routine_items " +
            "WHERE id IN (:routineItemIds) AND cadence = :cadence " +
            "AND pendingRemovalAtMillis IS NOT NULL",
    )
    suspend fun deletePendingRemovals(
        cadence: String,
        routineItemIds: List<Long>,
    )

    @Query("DELETE FROM routine_items WHERE pendingRemovalAtMillis IS NOT NULL")
    suspend fun deleteAllPendingRemovals()

    @Query(
        "UPDATE routine_items SET position = :position " +
            "WHERE id = :routineItemId AND cadence = :cadence",
    )
    suspend fun updatePosition(
        cadence: String,
        routineItemId: Long,
        position: Int,
    )

    @Query("DELETE FROM routine_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
