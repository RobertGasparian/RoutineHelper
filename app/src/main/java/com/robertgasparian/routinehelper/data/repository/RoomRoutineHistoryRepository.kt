package com.robertgasparian.routinehelper.data.repository

import androidx.room.withTransaction
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.DailySnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.DailySnapshotWithEntries
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRoutineHistoryRepository(
    private val database: RoutineDatabase,
    private val dailySnapshotDao: DailySnapshotDao = database.dailySnapshotDao(),
) : RoutineHistoryRepository {
    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineDaySummary>> =
        (cadence?.let { dailySnapshotDao.snapshotsWithEntries(it.toStorageValue()) }
            ?: dailySnapshotDao.snapshotsWithEntries()).map { snapshots ->
            snapshots.map { snapshotWithEntries ->
                snapshotWithEntries.toSummary()
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        combine(
            dailySnapshotDao.snapshotHeader(snapshotId),
            dailySnapshotDao.snapshotEntries(snapshotId),
        ) { snapshot, entries ->
            snapshot?.toDomain(entries)
        }

    override fun snapshotForDate(
        date: String,
        cadence: RoutineCadence,
    ): Flow<RoutineDaySummary?> =
        dailySnapshotDao.snapshotForDate(date, cadence.toStorageValue()).map { snapshot ->
            snapshot?.let {
                RoutineDaySummary(
                    snapshotId = it.id,
                    date = it.date,
                    finalizedAtMillis = it.finalizedAtMillis,
                    cadence = it.cadence.toRoutineCadence(),
                    hasSummaryNote = !it.summaryNote.isNullOrBlank(),
                )
            }
        }

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
        summaryNote: String?,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val storageCadence = cadence.toStorageValue()
        val normalizedSummaryNote = summaryNote?.trim()?.takeIf(String::isNotEmpty)
        val existingSnapshot = dailySnapshotDao.snapshotForDateOnce(date, storageCadence)
        val snapshotId = existingSnapshot?.id ?: dailySnapshotDao.insertSnapshot(
            DailySnapshotEntity(
                date = date,
                finalizedAtMillis = finalizedAtMillis,
                cadence = storageCadence,
                summaryNote = normalizedSummaryNote,
            ),
        )
        if (existingSnapshot != null) {
            dailySnapshotDao.updateSnapshot(
                id = snapshotId,
                finalizedAtMillis = finalizedAtMillis,
                summaryNote = normalizedSummaryNote,
            )
            dailySnapshotDao.deleteEntries(snapshotId)
        }
        dailySnapshotDao.insertEntries(
            items.sortedBy { it.position }.map { item ->
                DailySnapshotEntryEntity(
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
        dailySnapshotDao.deleteSnapshot(snapshotId)
    }
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> "DAILY"
        RoutineCadence.Weekly -> "WEEKLY"
    }

private fun String.toRoutineCadence(): RoutineCadence =
    when (this) {
        "WEEKLY" -> RoutineCadence.Weekly
        else -> RoutineCadence.Daily
    }

private fun DailySnapshotEntity.toDomain(
    entries: List<DailySnapshotEntryEntity>,
): RoutineDaySnapshot =
    RoutineDaySnapshot(
        snapshotId = id,
        date = date,
        finalizedAtMillis = finalizedAtMillis,
        cadence = cadence.toRoutineCadence(),
        summaryNote = summaryNote,
        items = entries.toDomainItems(),
    )

private fun List<DailySnapshotEntryEntity>.toDomainItems(): List<RoutineDaySnapshotItem> =
    sortedBy { it.positionSnapshot }.map { entry ->
        RoutineDaySnapshotItem(
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

private fun DailySnapshotWithEntries.toSummary(): RoutineDaySummary {
    val countableEntries = entries.filterNot { entry -> entry.isHidden }
    return RoutineDaySummary(
        snapshotId = snapshot.id,
        date = snapshot.date,
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
