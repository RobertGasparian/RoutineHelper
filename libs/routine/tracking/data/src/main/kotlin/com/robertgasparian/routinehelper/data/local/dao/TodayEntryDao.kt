package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robertgasparian.routinehelper.data.local.entity.TodayEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayEntryDao {
    @Query("SELECT * FROM today_entries WHERE date = :date")
    fun entriesForDate(date: String): Flow<List<TodayEntryEntity>>

    @Query("SELECT * FROM today_entries WHERE date = :date AND routineItemId = :routineItemId")
    fun entryForDate(date: String, routineItemId: Long): Flow<TodayEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TodayEntryEntity): Long

    @Update
    suspend fun update(entry: TodayEntryEntity)

    @Query("DELETE FROM today_entries WHERE date = :date")
    suspend fun deleteEntriesForDate(date: String)

    @Query("DELETE FROM today_entries WHERE date < :date")
    suspend fun deleteEntriesBefore(date: String)
}
