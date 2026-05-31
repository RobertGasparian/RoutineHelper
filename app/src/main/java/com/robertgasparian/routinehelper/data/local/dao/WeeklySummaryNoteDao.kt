package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.WeeklySummaryNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklySummaryNoteDao {
    @Query("SELECT * FROM weekly_summary_notes WHERE weekStartDate = :weekStartDate")
    fun noteForWeek(weekStartDate: String): Flow<WeeklySummaryNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: WeeklySummaryNoteEntity)

    @Query("DELETE FROM weekly_summary_notes WHERE weekStartDate = :weekStartDate")
    suspend fun deleteForWeek(weekStartDate: String)
}
