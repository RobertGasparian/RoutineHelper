package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import kotlinx.coroutines.flow.Flow

interface RoutineHistoryRepository {
    fun snapshotSummaries(): Flow<List<RoutineDaySummary>>

    fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?>

    fun snapshotForDate(date: String): Flow<RoutineDaySummary?>

    suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
    ): Long

    suspend fun deleteSnapshot(snapshotId: Long)
}
