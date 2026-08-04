package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionTagSelectionEntity
import com.robertgasparian.routinehelper.data.local.model.WeeklyReflectionWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReflectionDao {
    @Transaction
    @Query("SELECT * FROM weekly_reflections WHERE weekStartDate = :weekStartDate")
    fun reflectionForWeek(weekStartDate: String): Flow<WeeklyReflectionWithTags?>

    @Upsert
    suspend fun upsert(reflection: WeeklyReflectionEntity)

    @Insert
    suspend fun insertTagSelections(selections: List<WeeklyReflectionTagSelectionEntity>)

    @Query("DELETE FROM weekly_reflection_tag_selections WHERE weekStartDate = :weekStartDate")
    suspend fun deleteTagSelectionsForWeek(weekStartDate: String)

    @Query("DELETE FROM weekly_reflections WHERE weekStartDate = :weekStartDate")
    suspend fun deleteForWeek(weekStartDate: String)
}
