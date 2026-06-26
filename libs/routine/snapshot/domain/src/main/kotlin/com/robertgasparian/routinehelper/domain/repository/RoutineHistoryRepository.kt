package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
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
        summaryNote: String?,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ): Long

    suspend fun deleteSnapshot(snapshotId: Long)
}
