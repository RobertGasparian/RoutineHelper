package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import kotlinx.coroutines.flow.Flow

interface RoutineHistoryRepository {
    fun snapshotSummaries(cadence: RoutineCadence? = null): Flow<List<RoutineDaySummary>>

    fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?>

    suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ): Long

    suspend fun deleteSnapshot(snapshotId: Long)
}
