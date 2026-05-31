package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.DailySummaryNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryNoteDao {
    @Query("SELECT * FROM daily_summary_notes WHERE date = :date")
    fun noteForDate(date: String): Flow<DailySummaryNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: DailySummaryNoteEntity)

    @Query("DELETE FROM daily_summary_notes WHERE date = :date")
    suspend fun deleteForDate(date: String)
}
