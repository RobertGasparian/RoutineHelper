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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRoutineHistoryRepository(
    private val database: RoutineDatabase,
    private val dailySnapshotDao: DailySnapshotDao = database.dailySnapshotDao(),
) : RoutineHistoryRepository {
    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineDaySummary>> =
        (cadence?.let { dailySnapshotDao.snapshots(it.toStorageValue()) } ?: dailySnapshotDao.snapshots()).map { snapshots ->
            snapshots.map { snapshot ->
                RoutineDaySummary(
                    snapshotId = snapshot.id,
                    date = snapshot.date,
                    finalizedAtMillis = snapshot.finalizedAtMillis,
                    cadence = snapshot.cadence.toRoutineCadence(),
                )
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        dailySnapshotDao.snapshot(snapshotId).map { snapshotWithEntries ->
            snapshotWithEntries?.toDomain()
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
                )
            }
        }

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val snapshotId = dailySnapshotDao.insertSnapshot(
            DailySnapshotEntity(
                date = date,
                finalizedAtMillis = finalizedAtMillis,
                cadence = cadence.toStorageValue(),
            ),
        )
        dailySnapshotDao.insertEntries(
            items.sortedBy { it.position }.map { item ->
                DailySnapshotEntryEntity(
                    snapshotId = snapshotId,
                    actionId = item.actionId,
                    titleSnapshot = item.title,
                    descriptionSnapshot = item.description,
                    positionSnapshot = item.position,
                    isChecked = item.isChecked,
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

private fun DailySnapshotWithEntries.toDomain(): RoutineDaySnapshot =
    RoutineDaySnapshot(
        snapshotId = snapshot.id,
        date = snapshot.date,
        finalizedAtMillis = snapshot.finalizedAtMillis,
        cadence = snapshot.cadence.toRoutineCadence(),
        items = entries
            .sortedBy { it.positionSnapshot }
            .map { entry ->
                RoutineDaySnapshotItem(
                    actionId = entry.actionId,
                    title = entry.titleSnapshot,
                    description = entry.descriptionSnapshot,
                    position = entry.positionSnapshot,
                    isChecked = entry.isChecked,
                    note = entry.note,
                )
            },
    )
