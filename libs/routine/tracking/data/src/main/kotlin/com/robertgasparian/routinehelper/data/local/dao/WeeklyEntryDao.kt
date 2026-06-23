package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyEntryDao {
    @Query("SELECT * FROM weekly_entries WHERE weekStartDate = :weekStartDate")
    fun entriesForWeek(weekStartDate: String): Flow<List<WeeklyEntryEntity>>

    @Query(
        """
        INSERT INTO weekly_entries (
            routineItemId,
            weekStartDate,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :weekStartDate, :isChecked, 0, 0, NULL, :updatedAtMillis)
        ON CONFLICT(weekStartDate, routineItemId) DO UPDATE SET
            isChecked = :isChecked,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO weekly_entries (
            routineItemId,
            weekStartDate,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :weekStartDate, 0, 0, 0, :note, :updatedAtMillis)
        ON CONFLICT(weekStartDate, routineItemId) DO UPDATE SET
            note = :note,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO weekly_entries (
            routineItemId,
            weekStartDate,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :weekStartDate, 0, :completedCount, 0, NULL, :updatedAtMillis)
        ON CONFLICT(weekStartDate, routineItemId) DO UPDATE SET
            completedCount = :completedCount,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
        updatedAtMillis: Long,
    )

    @Query(
        """
        INSERT INTO weekly_entries (
            routineItemId,
            weekStartDate,
            isChecked,
            completedCount,
            isHidden,
            note,
            updatedAtMillis
        )
        VALUES (:routineItemId, :weekStartDate, 0, 0, :isHidden, NULL, :updatedAtMillis)
        ON CONFLICT(weekStartDate, routineItemId) DO UPDATE SET
            isHidden = :isHidden,
            updatedAtMillis = :updatedAtMillis
        """,
    )
    suspend fun upsertHidden(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM weekly_entries WHERE weekStartDate = :weekStartDate")
    suspend fun deleteEntriesForWeek(weekStartDate: String)
}
