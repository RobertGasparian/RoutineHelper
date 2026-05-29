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
    @Query("SELECT * FROM routine_items ORDER BY position")
    fun observeRoutineItems(): Flow<List<RoutineItemWithAction>>

    @Transaction
    @Query("SELECT * FROM routine_items ORDER BY position")
    suspend fun getRoutineItems(): List<RoutineItemWithAction>

    @Insert
    suspend fun insert(routineItem: RoutineItemEntity): Long

    @Update
    suspend fun update(routineItem: RoutineItemEntity)

    @Query("DELETE FROM routine_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
