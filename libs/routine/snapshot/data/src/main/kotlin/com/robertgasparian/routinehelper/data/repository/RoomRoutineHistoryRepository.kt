package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineSnapshotWithEntries
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRoutineHistoryRepository @Inject constructor(
    private val database: RoomDatabase,
    private val routineSnapshotDao: RoutineSnapshotDao,
) : RoutineHistoryRepository {
    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineSnapshotSummary>> =
        (cadence?.let { routineSnapshotDao.snapshotsWithEntries(it.toStorageValue()) }
            ?: routineSnapshotDao.snapshotsWithEntries()).map { snapshots ->
            snapshots.map { snapshotWithEntries ->
                snapshotWithEntries.toSummary()
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineSnapshot?> =
        routineSnapshotDao.snapshot(snapshotId).map { snapshotWithEntries ->
            snapshotWithEntries?.toDomain()
        }

    override suspend fun saveSnapshot(
        periodStartDate: String,
        finalizedAtMillis: Long,
        items: List<RoutineSnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val storageCadence = cadence.toStorageValue()
        val normalizedSummaryNote = summaryNote?.trim()?.takeIf(String::isNotEmpty)
        val existingSnapshot = routineSnapshotDao.snapshotForPeriodStartDateOnce(periodStartDate, storageCadence)
        val snapshotId = existingSnapshot?.id ?: routineSnapshotDao.insertSnapshot(
            RoutineSnapshotEntity(
                periodStartDate = periodStartDate,
                finalizedAtMillis = finalizedAtMillis,
                cadence = storageCadence,
                summaryNote = normalizedSummaryNote,
            ),
        )
        if (existingSnapshot != null) {
            routineSnapshotDao.updateSnapshot(
                id = snapshotId,
                finalizedAtMillis = finalizedAtMillis,
                summaryNote = normalizedSummaryNote,
            )
            routineSnapshotDao.deleteEntries(snapshotId)
        }
        routineSnapshotDao.insertEntries(
            items.sortedBy { it.position }.map { item ->
                RoutineSnapshotEntryEntity(
                    snapshotId = snapshotId,
                    actionId = item.actionId,
                    titleSnapshot = item.title,
                    descriptionSnapshot = item.description,
                    positionSnapshot = item.position,
                    isChecked = item.isChecked,
                    isHidden = item.isHidden,
                    repeatTargetCountSnapshot = item.repeatTargetCount,
                    completedCount = item.completedCount,
                    note = item.note,
                )
            },
        )
        snapshotId
    }

    override suspend fun deleteSnapshot(snapshotId: Long) {
        routineSnapshotDao.deleteSnapshot(snapshotId)
    }
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> RoutineSnapshotEntity.DAILY_CADENCE_STORAGE_VALUE
        RoutineCadence.Weekly -> RoutineSnapshotEntity.WEEKLY_CADENCE_STORAGE_VALUE
    }

private fun String.toRoutineCadence(): RoutineCadence =
    when (this) {
        RoutineSnapshotEntity.DAILY_CADENCE_STORAGE_VALUE -> RoutineCadence.Daily
        RoutineSnapshotEntity.WEEKLY_CADENCE_STORAGE_VALUE -> RoutineCadence.Weekly
        else -> error("Unsupported routine cadence storage value: $this")
    }

private fun RoutineSnapshotWithEntries.toDomain(): RoutineSnapshot =
    RoutineSnapshot(
        snapshotId = snapshot.id,
        periodStartDate = snapshot.periodStartDate,
        finalizedAtMillis = snapshot.finalizedAtMillis,
        cadence = snapshot.cadence.toRoutineCadence(),
        summaryNote = snapshot.summaryNote,
        items = entries.toDomainItems(),
    )

private fun List<RoutineSnapshotEntryEntity>.toDomainItems(): List<RoutineSnapshotItem> =
    sortedBy { it.positionSnapshot }.map { entry ->
        RoutineSnapshotItem(
            actionId = entry.actionId,
            title = entry.titleSnapshot,
            description = entry.descriptionSnapshot,
            repeatTargetCount = entry.repeatTargetCountSnapshot,
            completedCount = entry.completedCount,
            position = entry.positionSnapshot,
            isChecked = entry.isChecked,
            isHidden = entry.isHidden,
            note = entry.note,
        )
    }

private fun RoutineSnapshotWithEntries.toSummary(): RoutineSnapshotSummary {
    val countableEntries = entries.filterNot { entry -> entry.isHidden }
    return RoutineSnapshotSummary(
        snapshotId = snapshot.id,
        periodStartDate = snapshot.periodStartDate,
        finalizedAtMillis = snapshot.finalizedAtMillis,
        cadence = snapshot.cadence.toRoutineCadence(),
        completedCount = countableEntries.count { entry ->
            entry.repeatTargetCountSnapshot?.let { target ->
                entry.completedCount >= target
            } ?: entry.isChecked
        },
        totalCount = countableEntries.size,
        hasSummaryNote = !snapshot.summaryNote.isNullOrBlank(),
    )
}
