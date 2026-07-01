package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM actions ORDER BY title COLLATE NOCASE")
    fun actions(): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id")
    fun action(id: Long): Flow<ActionEntity?>

    @Insert
    suspend fun insert(action: ActionEntity): Long

    @Update
    suspend fun update(action: ActionEntity)

    @Query("DELETE FROM actions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
