package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.CurrentListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentListItemDao {
    @Query("SELECT * FROM current_list_items WHERE pendingRemovalAtMillis IS NULL ORDER BY position, id")
    fun currentListItems(): Flow<List<CurrentListItemEntity>>

    @Query("SELECT * FROM current_list_items ORDER BY position, id")
    suspend fun allCurrentListItemsSnapshot(): List<CurrentListItemEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM current_list_items")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(item: CurrentListItemEntity): Long

    @Query("UPDATE current_list_items SET isChecked = :isChecked, updatedAtMillis = :updatedAtMillis WHERE id = :itemId")
    suspend fun updateChecked(
        itemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    )

    @Query("UPDATE current_list_items SET isChecked = :isChecked, updatedAtMillis = :updatedAtMillis WHERE pendingRemovalAtMillis IS NULL")
    suspend fun updateAllChecked(
        isChecked: Boolean,
        updatedAtMillis: Long,
    )

    @Query("UPDATE current_list_items SET position = :position, updatedAtMillis = :updatedAtMillis WHERE id = :itemId")
    suspend fun updatePosition(
        itemId: Long,
        position: Int,
        updatedAtMillis: Long,
    )

    @Query("UPDATE current_list_items SET pendingRemovalAtMillis = :pendingRemovalAtMillis, updatedAtMillis = :pendingRemovalAtMillis WHERE id = :itemId")
    suspend fun markPendingRemoval(
        itemId: Long,
        pendingRemovalAtMillis: Long,
    )

    @Query("UPDATE current_list_items SET pendingRemovalAtMillis = NULL, updatedAtMillis = :updatedAtMillis WHERE id = :itemId")
    suspend fun restorePendingRemoval(
        itemId: Long,
        updatedAtMillis: Long,
    )

    @Query("UPDATE current_list_items SET pendingRemovalAtMillis = NULL, updatedAtMillis = :updatedAtMillis WHERE id IN (:itemIds)")
    suspend fun restorePendingRemovals(
        itemIds: List<Long>,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM current_list_items WHERE id IN (:itemIds) AND pendingRemovalAtMillis IS NOT NULL")
    suspend fun deletePendingRemovals(itemIds: List<Long>)

    @Query("DELETE FROM current_list_items WHERE pendingRemovalAtMillis IS NOT NULL")
    suspend fun deleteAllPendingRemovals()

    @Query("DELETE FROM current_list_items")
    suspend fun deleteAll()
}
