package com.robertgasparian.routinehelper.data.repository

import androidx.room.withTransaction
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.DailySnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.DailySnapshotWithEntries
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
    override fun snapshotSummaries(): Flow<List<RoutineDaySummary>> =
        dailySnapshotDao.snapshots().map { snapshots ->
            snapshots.map { snapshot ->
                RoutineDaySummary(
                    snapshotId = snapshot.id,
                    date = snapshot.date,
                    finalizedAtMillis = snapshot.finalizedAtMillis,
                )
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        dailySnapshotDao.snapshot(snapshotId).map { snapshotWithEntries ->
            snapshotWithEntries?.toDomain()
        }

    override fun snapshotForDate(date: String): Flow<RoutineDaySummary?> =
        dailySnapshotDao.snapshotForDate(date).map { snapshot ->
            snapshot?.let {
                RoutineDaySummary(
                    snapshotId = it.id,
                    date = it.date,
                    finalizedAtMillis = it.finalizedAtMillis,
                )
            }
        }

    override suspend fun saveSnapshot(
        date: String,
        finalizedAtMillis: Long,
        items: List<RoutineDaySnapshotItem>,
    ): Long = database.withTransaction {
        val snapshotId = dailySnapshotDao.insertSnapshot(
            DailySnapshotEntity(
                date = date,
                finalizedAtMillis = finalizedAtMillis,
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
}

private fun DailySnapshotWithEntries.toDomain(): RoutineDaySnapshot =
    RoutineDaySnapshot(
        snapshotId = snapshot.id,
        date = snapshot.date,
        finalizedAtMillis = snapshot.finalizedAtMillis,
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
