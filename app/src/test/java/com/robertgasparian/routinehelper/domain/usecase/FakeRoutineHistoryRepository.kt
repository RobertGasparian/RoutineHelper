package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
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
    val deletedSnapshotIds = mutableListOf<Long>()

    fun setSnapshot(summary: RoutineDaySummary) {
        snapshots.value = snapshots.value + RoutineDaySnapshot(
            snapshotId = summary.snapshotId,
            date = summary.date,
            finalizedAtMillis = summary.finalizedAtMillis,
            cadence = summary.cadence,
            items = emptyList(),
        )
    }

    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineDaySummary>> =
        snapshots.map { snapshotList ->
            snapshotList.filter { snapshot -> cadence == null || snapshot.cadence == cadence }.map { snapshot ->
                RoutineDaySummary(
                    snapshotId = snapshot.snapshotId,
                    date = snapshot.date,
                    finalizedAtMillis = snapshot.finalizedAtMillis,
                    cadence = snapshot.cadence,
                )
            }
        }

    fun snapshotSummaries(): Flow<List<RoutineDaySummary>> = snapshotSummaries(cadence = null)

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        snapshots.map { snapshotList ->
            snapshotList.firstOrNull { it.snapshotId == snapshotId }
        }

    override fun snapshotForDate(
        date: String,
        cadence: RoutineCadence,
    ): Flow<RoutineDaySummary?> =
        snapshotSummaries().map { summaries ->
            summaries.firstOrNull { it.date == date && it.cadence == cadence }
        }

    fun snapshotForDate(date: String): Flow<RoutineDaySummary?> =
        snapshotForDate(date = date, cadence = RoutineCadence.Daily)

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence,
    ): Long {
        val snapshotId = savedSnapshots.size + 1L
        savedSnapshots += SavedSnapshot(
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
            summaryNote = summaryNote,
            cadence = cadence,
        )
        snapshots.value = snapshots.value + RoutineDaySnapshot(
            snapshotId = snapshotId,
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            cadence = cadence,
            summaryNote = summaryNote,
            items = items,
        )
        return snapshotId
    }

    suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
    ): Long =
        saveSnapshot(
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
            summaryNote = null,
            cadence = RoutineCadence.Daily,
        )

    override suspend fun deleteSnapshot(snapshotId: Long) {
        deletedSnapshotIds += snapshotId
        snapshots.value = snapshots.value.filterNot { it.snapshotId == snapshotId }
    }
}

data class SavedSnapshot(
    val date: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineDaySnapshotItem>,
    val summaryNote: String? = null,
    val cadence: RoutineCadence = RoutineCadence.Daily,
)
