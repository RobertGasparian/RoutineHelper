package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRoutineHistoryRepository : RoutineHistoryRepository {
    private val snapshots = MutableStateFlow<List<RoutineSnapshot>>(emptyList())
    private val summaries = MutableStateFlow<List<RoutineSnapshotSummary>>(emptyList())
    val savedSnapshots = mutableListOf<SavedSnapshot>()
    val deletedSnapshotIds = mutableListOf<Long>()

    fun setSnapshot(summary: RoutineSnapshotSummary) {
        summaries.value = summaries.value.replaceById(summary)
        snapshots.value = snapshots.value.replaceById(
            RoutineSnapshot(
                snapshotId = summary.snapshotId,
                periodStartDate = summary.periodStartDate,
                finalizedAtMillis = summary.finalizedAtMillis,
                cadence = summary.cadence,
                items = emptyList(),
                summaryNote = "Summary note".takeIf { summary.hasSummaryNote },
            ),
        )
    }

    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineSnapshotSummary>> =
        summaries.map { summaryList ->
            summaryList.filter { summary -> cadence == null || summary.cadence == cadence }
        }

    fun snapshotSummaries(): Flow<List<RoutineSnapshotSummary>> = snapshotSummaries(cadence = null)

    override fun snapshot(snapshotId: Long): Flow<RoutineSnapshot?> =
        snapshots.map { snapshotList ->
            snapshotList.firstOrNull { it.snapshotId == snapshotId }
        }

    override suspend fun saveSnapshot(
        periodStartDate: String,
        finalizedAtMillis: Long,
        items: List<RoutineSnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence,
    ): Long {
        val existingSnapshot = snapshots.value.firstOrNull { snapshot ->
            snapshot.periodStartDate == periodStartDate && snapshot.cadence == cadence
        }
        val snapshotId =
            existingSnapshot?.snapshotId ?: ((snapshots.value.maxOfOrNull { it.snapshotId }
                ?: 0L) + 1L)
        val savedSnapshot = SavedSnapshot(
            periodStartDate = periodStartDate,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
            summaryNote = summaryNote,
            cadence = cadence,
        )
        savedSnapshots.removeAll { snapshot -> snapshot.periodStartDate == periodStartDate && snapshot.cadence == cadence }
        savedSnapshots += savedSnapshot
        val snapshot = RoutineSnapshot(
            snapshotId = snapshotId,
            periodStartDate = periodStartDate,
            finalizedAtMillis = finalizedAtMillis,
            cadence = cadence,
            summaryNote = summaryNote,
            items = items,
        )
        snapshots.value = snapshots.value.filterNot { snapshot ->
            snapshot.periodStartDate == periodStartDate && snapshot.cadence == cadence
        } + snapshot
        summaries.value = summaries.value.filterNot { summary ->
            summary.periodStartDate == periodStartDate && summary.cadence == cadence
        } + snapshot.toSummary()
        return snapshotId
    }

    suspend fun saveSnapshot(
        periodStartDate: String,
        finalizedAtMillis: Long,
        items: List<RoutineSnapshotItem>,
        cadence: RoutineCadence,
    ): Long =
        saveSnapshot(
            periodStartDate = periodStartDate,
            finalizedAtMillis = finalizedAtMillis,
            items = items,
            summaryNote = null,
            cadence = cadence,
        )

    override suspend fun deleteSnapshot(snapshotId: Long) {
        deletedSnapshotIds += snapshotId
        snapshots.value = snapshots.value.filterNot { it.snapshotId == snapshotId }
        summaries.value = summaries.value.filterNot { it.snapshotId == snapshotId }
    }
}

private fun List<RoutineSnapshot>.replaceById(snapshot: RoutineSnapshot): List<RoutineSnapshot> =
    filterNot { it.snapshotId == snapshot.snapshotId } + snapshot

private fun List<RoutineSnapshotSummary>.replaceById(summary: RoutineSnapshotSummary): List<RoutineSnapshotSummary> =
    filterNot { it.snapshotId == summary.snapshotId } + summary

private fun RoutineSnapshot.toSummary(): RoutineSnapshotSummary {
    val countableItems = items.filterNot(RoutineSnapshotItem::isHidden)
    return RoutineSnapshotSummary(
        snapshotId = snapshotId,
        periodStartDate = periodStartDate,
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
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineSnapshotItem>,
    val cadence: RoutineCadence,
    val summaryNote: String? = null,
)
