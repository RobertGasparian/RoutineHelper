package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun entriesForDate(date: String): Flow<List<DailyEntryEntity>>

    @Query(
        """
        INSERT INTO daily_entries (
            routineItemId,
            date,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :date, :isChecked, 0, 0, NULL, :updatedAtMillis)
        ON CONFLICT(date, routineItemId) DO UPDATE SET
            isChecked = :isChecked,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO daily_entries (
            routineItemId,
            date,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :date, 0, 0, 0, :note, :updatedAtMillis)
        ON CONFLICT(date, routineItemId) DO UPDATE SET
            note = :note,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertNote(
        date: String,
        routineItemId: Long,
        note: String?,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO daily_entries (
            routineItemId,
            date,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :date, 0, :completedCount, 0, NULL, :updatedAtMillis)
        ON CONFLICT(date, routineItemId) DO UPDATE SET
            completedCount = :completedCount,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertCompletedCount(
        date: String,
        routineItemId: Long,
        completedCount: Int,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO daily_entries (
            routineItemId,
            date,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :date, 0, 0, :isHidden, NULL, :updatedAtMillis)
        ON CONFLICT(date, routineItemId) DO UPDATE SET
            isHidden = :isHidden,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertHidden(
        date: String,
        routineItemId: Long,
        isHidden: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteEntriesForDate(date: String)
}
