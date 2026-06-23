package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.DailySnapshotWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySnapshotDao {
    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC")
    fun snapshots(): Flow<List<DailySnapshotEntity>>

    @Query("SELECT * FROM daily_snapshots WHERE cadence = :cadence ORDER BY date DESC")
    fun snapshots(cadence: String): Flow<List<DailySnapshotEntity>>

    @Transaction
    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC")
    fun snapshotsWithEntries(): Flow<List<DailySnapshotWithEntries>>

    @Transaction
    @Query("SELECT * FROM daily_snapshots WHERE cadence = :cadence ORDER BY date DESC")
    fun snapshotsWithEntries(cadence: String): Flow<List<DailySnapshotWithEntries>>

    @Transaction
    @Query("SELECT * FROM daily_snapshots WHERE id = :id")
    fun snapshot(id: Long): Flow<DailySnapshotWithEntries?>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date AND cadence = :cadence")
    fun snapshotForDate(
        date: String,
        cadence: String,
    ): Flow<DailySnapshotEntity?>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date AND cadence = :cadence")
    suspend fun snapshotForDateOnce(
        date: String,
        cadence: String,
    ): DailySnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSnapshot(snapshot: DailySnapshotEntity): Long

    @Query(
        """
        UPDATE daily_snapshots
        SET finalizedAtMillis = :finalizedAtMillis,
            summaryNote = :summaryNote
        WHERE id = :id
        """,
    )
    suspend fun updateSnapshot(
        id: Long,
        finalizedAtMillis: Long,
        summaryNote: String?,
    )

    @Insert
    suspend fun insertEntries(entries: List<DailySnapshotEntryEntity>)

    @Query("DELETE FROM daily_snapshot_entries WHERE snapshotId = :snapshotId")
    suspend fun deleteEntries(snapshotId: Long)

    @Query("DELETE FROM daily_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)
}
