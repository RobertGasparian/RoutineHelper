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
    fun observeSnapshots(): Flow<List<DailySnapshotEntity>>

    @Transaction
    @Query("SELECT * FROM daily_snapshots WHERE id = :id")
    fun observeSnapshot(id: Long): Flow<DailySnapshotWithEntries?>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date")
    suspend fun getSnapshotForDate(date: String): DailySnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSnapshot(snapshot: DailySnapshotEntity): Long

    @Insert
    suspend fun insertEntries(entries: List<DailySnapshotEntryEntity>)

    @Query("DELETE FROM daily_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)
}
