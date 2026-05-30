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
    @Query("SELECT * FROM daily_snapshots WHERE id = :id")
    fun snapshot(id: Long): Flow<DailySnapshotWithEntries?>

    @Query("SELECT * FROM daily_snapshot_entries WHERE snapshotId = :snapshotId ORDER BY positionSnapshot")
    fun snapshotEntries(snapshotId: Long): Flow<List<DailySnapshotEntryEntity>>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date AND cadence = :cadence")
    fun snapshotForDate(
        date: String,
        cadence: String,
    ): Flow<DailySnapshotEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSnapshot(snapshot: DailySnapshotEntity): Long

    @Insert
    suspend fun insertEntries(entries: List<DailySnapshotEntryEntity>)

    @Query("DELETE FROM daily_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)
}
