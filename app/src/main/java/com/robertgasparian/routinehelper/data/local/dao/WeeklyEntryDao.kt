package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyEntryDao {
    @Query("SELECT * FROM weekly_entries WHERE weekStartDate = :weekStartDate")
    fun entriesForWeek(weekStartDate: String): Flow<List<WeeklyEntryEntity>>

    @Query(
        "SELECT * FROM weekly_entries WHERE weekStartDate = :weekStartDate AND routineItemId = :routineItemId",
    )
    fun entryForWeek(
        weekStartDate: String,
        routineItemId: Long,
    ): Flow<WeeklyEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeeklyEntryEntity): Long

    @Update
    suspend fun update(entry: WeeklyEntryEntity)

    @Query("DELETE FROM weekly_entries WHERE weekStartDate = :weekStartDate")
    suspend fun deleteEntriesForWeek(weekStartDate: String)
}
