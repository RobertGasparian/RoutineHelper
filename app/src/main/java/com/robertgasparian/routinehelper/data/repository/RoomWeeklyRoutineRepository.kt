package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class RoomWeeklyRoutineRepository(
    database: RoutineDatabase,
    private val routineItemDao: RoutineItemDao = database.routineItemDao(),
    private val weeklyEntryDao: WeeklyEntryDao = database.weeklyEntryDao(),
    private val clock: () -> Long = System::currentTimeMillis,
) : WeeklyRoutineRepository {
    override fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>> =
        combine(
            routineItemDao.routineItems(RoutineCadence.Weekly.toStorageValue()),
            weeklyEntryDao.entriesForWeek(weekStartDate),
        ) { routineItems, weeklyEntries ->
            val entriesByRoutineItemId = weeklyEntries.associateBy { it.routineItemId }
            routineItems.map { routineItem ->
                routineItem.toWeeklyDomain(
                    weekStartDate = weekStartDate,
                    weeklyEntry = entriesByRoutineItemId[routineItem.routineItem.id],
                )
            }
        }

    override suspend fun setChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        val existing = weeklyEntryDao.entryForWeek(weekStartDate, routineItemId).first()
        weeklyEntryDao.upsert(
            existing?.copy(
                isChecked = isChecked,
                updatedAtMillis = clock(),
            ) ?: WeeklyEntryEntity(
                routineItemId = routineItemId,
                weekStartDate = weekStartDate,
                isChecked = isChecked,
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun updateNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
    ) {
        val normalizedNote = note?.trim()?.takeIf(String::isNotEmpty)
        val existing = weeklyEntryDao.entryForWeek(weekStartDate, routineItemId).first()
        weeklyEntryDao.upsert(
            existing?.copy(
                note = normalizedNote,
                updatedAtMillis = clock(),
            ) ?: WeeklyEntryEntity(
                routineItemId = routineItemId,
                weekStartDate = weekStartDate,
                note = normalizedNote,
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun updateCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        val existing = weeklyEntryDao.entryForWeek(weekStartDate, routineItemId).first()
        weeklyEntryDao.upsert(
            existing?.copy(
                completedCount = completedCount.coerceAtLeast(0),
                updatedAtMillis = clock(),
            ) ?: WeeklyEntryEntity(
                routineItemId = routineItemId,
                weekStartDate = weekStartDate,
                completedCount = completedCount.coerceAtLeast(0),
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun resetWeek(weekStartDate: String) {
        weeklyEntryDao.deleteEntriesForWeek(weekStartDate)
    }
}

private fun RoutineItemWithAction.toWeeklyDomain(
    weekStartDate: String,
    weeklyEntry: WeeklyEntryEntity?,
): WeeklyRoutineItem {
    val completedCount = action.repeatTargetCount?.let { targetCount ->
        (weeklyEntry?.completedCount ?: 0).coerceIn(0, targetCount)
    } ?: 0
    return WeeklyRoutineItem(
        routineItemId = routineItem.id,
        actionId = action.id,
        title = action.title,
        description = action.description,
        repeatTargetCount = action.repeatTargetCount,
        completedCount = completedCount,
        position = routineItem.position,
        weekStartDate = weekStartDate,
        isChecked = action.repeatTargetCount?.let { targetCount ->
            completedCount >= targetCount
        } ?: (weeklyEntry?.isChecked ?: false),
        note = weeklyEntry?.note,
    )
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> "DAILY"
        RoutineCadence.Weekly -> "WEEKLY"
    }
