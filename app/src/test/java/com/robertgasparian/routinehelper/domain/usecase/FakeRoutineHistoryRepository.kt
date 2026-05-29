package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRoutineHistoryRepository : RoutineHistoryRepository {
    private val snapshots = MutableStateFlow<List<RoutineDaySnapshot>>(emptyList())
    val savedSnapshots = mutableListOf<SavedSnapshot>()

    fun setSnapshot(summary: RoutineDaySummary) {
        snapshots.value = snapshots.value + RoutineDaySnapshot(
            snapshotId = summary.snapshotId,
            date = summary.date,
            finalizedAtMillis = summary.finalizedAtMillis,
            items = emptyList(),
        )
    }

    override fun snapshotSummaries(): Flow<List<RoutineDaySummary>> =
        snapshots.map { snapshotList ->
            snapshotList.map { snapshot ->
                RoutineDaySummary(
                    snapshotId = snapshot.snapshotId,
                    date = snapshot.date,
                    finalizedAtMillis = snapshot.finalizedAtMillis,
                )
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        snapshots.map { snapshotList ->
            snapshotList.firstOrNull { it.snapshotId == snapshotId }
        }

    override fun snapshotForDate(date: String): Flow<RoutineDaySummary?> =
        snapshotSummaries().map { summaries ->
            summaries.firstOrNull { it.date == date }
        }

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
    ): Long {
        val snapshotId = savedSnapshots.size + 1L
        savedSnapshots += SavedSnapshot(
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
        )
        snapshots.value = snapshots.value + RoutineDaySnapshot(
            snapshotId = snapshotId,
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
        )
        return snapshotId
    }
}

data class SavedSnapshot(
    val date: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineDaySnapshotItem>,
)
