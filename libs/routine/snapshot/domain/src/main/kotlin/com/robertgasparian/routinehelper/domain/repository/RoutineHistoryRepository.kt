package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import kotlinx.coroutines.flow.Flow

interface RoutineHistoryRepository {
    fun snapshotSummaries(cadence: RoutineCadence? = null): Flow<List<RoutineSnapshotSummary>>

    fun snapshot(snapshotId: Long): Flow<RoutineSnapshot?>

    suspend fun saveSnapshot(
        periodStartDate: String,
        finalizedAtMillis: Long,
        items: List<RoutineSnapshotItem>,
        reflection: RoutineReflection = RoutineReflection(),
        cadence: RoutineCadence,
    ): Long

    /**
     * Focused reflection command endpoint. Keep it separate from whole-snapshot writes so a future
     * editability policy can guard this action without risking unrelated snapshot fields.
     */
    suspend fun updateSnapshotReflection(
        snapshotId: Long,
        reflection: RoutineReflection,
    )

    suspend fun deleteSnapshot(snapshotId: Long)
}
