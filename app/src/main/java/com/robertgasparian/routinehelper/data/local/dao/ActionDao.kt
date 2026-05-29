package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM actions ORDER BY title COLLATE NOCASE")
    fun observeActions(): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getAction(id: Long): ActionEntity?

    @Insert
    suspend fun insert(action: ActionEntity): Long

    @Update
    suspend fun update(action: ActionEntity)

    @Delete
    suspend fun delete(action: ActionEntity)
}
