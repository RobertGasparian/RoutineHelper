package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionTagSelectionEntity
import com.robertgasparian.routinehelper.data.local.model.DailyReflectionWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {
    @Transaction
    @Query("SELECT * FROM daily_reflections WHERE date = :date")
    fun reflectionForDate(date: String): Flow<DailyReflectionWithTags?>

    @Upsert
    suspend fun upsert(reflection: DailyReflectionEntity)

    @Insert
    suspend fun insertTagSelections(selections: List<DailyReflectionTagSelectionEntity>)

    @Query("DELETE FROM daily_reflection_tag_selections WHERE date = :date")
    suspend fun deleteTagSelectionsForDate(date: String)

    @Query("DELETE FROM daily_reflections WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
