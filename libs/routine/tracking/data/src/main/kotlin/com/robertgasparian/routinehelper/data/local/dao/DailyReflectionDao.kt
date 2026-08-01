package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE date = :date")
    fun reflectionForDate(date: String): Flow<DailyReflectionEntity?>

    @Upsert
    suspend fun upsert(reflection: DailyReflectionEntity)

    @Query("DELETE FROM daily_reflections WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
