package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotReflectionTagEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineSnapshotWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineSnapshotDao {
    @Transaction
    @Query("SELECT * FROM routine_snapshots ORDER BY periodStartDate DESC")
    fun snapshotsWithEntries(): Flow<List<RoutineSnapshotWithEntries>>

    @Transaction
    @Query("SELECT * FROM routine_snapshots WHERE cadence = :cadence ORDER BY periodStartDate DESC")
    fun snapshotsWithEntries(cadence: String): Flow<List<RoutineSnapshotWithEntries>>

    @Transaction
    @Query("SELECT * FROM routine_snapshots WHERE id = :id")
    fun snapshot(id: Long): Flow<RoutineSnapshotWithEntries?>

    @Query("SELECT * FROM routine_snapshots WHERE periodStartDate = :periodStartDate AND cadence = :cadence")
    suspend fun snapshotForPeriodStartDateOnce(
        periodStartDate: String,
        cadence: String,
    ): RoutineSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSnapshot(snapshot: RoutineSnapshotEntity): Long

    @Query(
        """
        UPDATE routine_snapshots
        SET finalizedAtMillis = :finalizedAtMillis,
            summaryNote = :summaryNote,
            rating = :rating
        WHERE id = :id
        """,
    )
    suspend fun updateSnapshot(
        id: Long,
        finalizedAtMillis: Long,
        summaryNote: String?,
        rating: Int?,
    )

    @Query(
        """
        UPDATE routine_snapshots
        SET summaryNote = :summaryNote,
            rating = :rating
        WHERE id = :snapshotId
        """,
    )
    suspend fun updateReflection(
        snapshotId: Long,
        summaryNote: String?,
        rating: Int?,
    )

    @Insert
    suspend fun insertEntries(entries: List<RoutineSnapshotEntryEntity>)

    @Insert
    suspend fun insertReflectionTags(tags: List<RoutineSnapshotReflectionTagEntity>)

    @Query("DELETE FROM routine_snapshot_entries WHERE snapshotId = :snapshotId")
    suspend fun deleteEntries(snapshotId: Long)

    @Query("DELETE FROM routine_snapshot_reflection_tags WHERE snapshotId = :snapshotId")
    suspend fun deleteReflectionTags(snapshotId: Long)

    @Query("DELETE FROM routine_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)
}
