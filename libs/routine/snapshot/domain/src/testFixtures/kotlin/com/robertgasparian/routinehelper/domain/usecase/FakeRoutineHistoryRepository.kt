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
    private val summaries = MutableStateFlow<List<RoutineDaySummary>>(emptyList())
    val savedSnapshots = mutableListOf<SavedSnapshot>()
    val deletedSnapshotIds = mutableListOf<Long>()

    fun setSnapshot(summary: RoutineDaySummary) {
        summaries.value = summaries.value.replaceById(summary)
        snapshots.value = snapshots.value.replaceById(
            RoutineDaySnapshot(
                snapshotId = summary.snapshotId,
                date = summary.date,
                finalizedAtMillis = summary.finalizedAtMillis,
                cadence = summary.cadence,
                items = emptyList(),
                summaryNote = "Summary note".takeIf { summary.hasSummaryNote },
            ),
        )
    }

    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineDaySummary>> =
        summaries.map { summaryList ->
            summaryList.filter { summary -> cadence == null || summary.cadence == cadence }
        }

    fun snapshotSummaries(): Flow<List<RoutineDaySummary>> = snapshotSummaries(cadence = null)

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        snapshots.map { snapshotList ->
            snapshotList.firstOrNull { it.snapshotId == snapshotId }
        }

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence,
    ): Long {
        val existingSnapshot = snapshots.value.firstOrNull { snapshot ->
            snapshot.date == date && snapshot.cadence == cadence
        }
        val snapshotId =
            existingSnapshot?.snapshotId ?: ((snapshots.value.maxOfOrNull { it.snapshotId }
                ?: 0L) + 1L)
        val savedSnapshot = SavedSnapshot(
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
            summaryNote = summaryNote,
            cadence = cadence,
        )
        savedSnapshots.removeAll { snapshot -> snapshot.date == date && snapshot.cadence == cadence }
        savedSnapshots += savedSnapshot
        val snapshot = RoutineDaySnapshot(
            snapshotId = snapshotId,
            date = date,
            finalizedAtMillis = finalizedAtMillis,
            cadence = cadence,
            summaryNote = summaryNote,
            items = items,
        )
        snapshots.value = snapshots.value.filterNot { snapshot ->
            snapshot.date == date && snapshot.cadence == cadence
        } + snapshot
        summaries.value = summaries.value.filterNot { summary ->
            summary.date == date && summary.cadence == cadence
        } + snapshot.toSummary()
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
        summaries.value = summaries.value.filterNot { it.snapshotId == snapshotId }
    }
}

private fun List<RoutineDaySnapshot>.replaceById(snapshot: RoutineDaySnapshot): List<RoutineDaySnapshot> =
    filterNot { it.snapshotId == snapshot.snapshotId } + snapshot

private fun List<RoutineDaySummary>.replaceById(summary: RoutineDaySummary): List<RoutineDaySummary> =
    filterNot { it.snapshotId == summary.snapshotId } + summary

private fun RoutineDaySnapshot.toSummary(): RoutineDaySummary {
    val countableItems = items.filterNot(RoutineDaySnapshotItem::isHidden)
    return RoutineDaySummary(
        snapshotId = snapshotId,
        date = date,
        finalizedAtMillis = finalizedAtMillis,
        cadence = cadence,
        completedCount = countableItems.count { item ->
            item.repeatTargetCount?.let { target ->
                item.completedCount >= target
            } ?: item.isChecked
        },
        totalCount = countableItems.size,
        hasSummaryNote = !summaryNote.isNullOrBlank(),
    )
}

data class SavedSnapshot(
    val date: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineDaySnapshotItem>,
    val summaryNote: String? = null,
    val cadence: RoutineCadence = RoutineCadence.Daily,
)
