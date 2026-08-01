package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReflectionDao {
    @Query("SELECT * FROM weekly_reflections WHERE weekStartDate = :weekStartDate")
    fun reflectionForWeek(weekStartDate: String): Flow<WeeklyReflectionEntity?>

    @Upsert
    suspend fun upsert(reflection: WeeklyReflectionEntity)

    @Query("DELETE FROM weekly_reflections WHERE weekStartDate = :weekStartDate")
    suspend fun deleteForWeek(weekStartDate: String)
}
