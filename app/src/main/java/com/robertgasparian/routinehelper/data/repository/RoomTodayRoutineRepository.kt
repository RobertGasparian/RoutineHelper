package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.TodayEntryDao
import com.robertgasparian.routinehelper.data.local.entity.TodayEntryEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class RoomTodayRoutineRepository(
    database: RoutineDatabase,
    private val routineItemDao: RoutineItemDao = database.routineItemDao(),
    private val todayEntryDao: TodayEntryDao = database.todayEntryDao(),
    private val clock: () -> Long = System::currentTimeMillis,
) : TodayRoutineRepository {
    override fun todayItems(
        date: String,
        cadence: RoutineCadence,
    ): Flow<List<TodayRoutineItem>> =
        combine(
            routineItemDao.routineItems(cadence.toStorageValue()),
            todayEntryDao.entriesForDate(date),
        ) { routineItems, todayEntries ->
            val entriesByRoutineItemId = todayEntries.associateBy { it.routineItemId }
            routineItems.map { routineItem ->
                routineItem.toTodayDomain(
                    date = date,
                    todayEntry = entriesByRoutineItemId[routineItem.routineItem.id],
                )
            }
        }

    override suspend fun setChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        val existing = todayEntryDao.entryForDate(date, routineItemId).first()
        todayEntryDao.upsert(
            existing?.copy(
                isChecked = isChecked,
                updatedAtMillis = clock(),
            ) ?: TodayEntryEntity(
                routineItemId = routineItemId,
                date = date,
                isChecked = isChecked,
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun updateNote(
        date: String,
        routineItemId: Long,
        note: String?,
    ) {
        val normalizedNote = note?.trim()?.takeIf(String::isNotEmpty)
        val existing = todayEntryDao.entryForDate(date, routineItemId).first()
        todayEntryDao.upsert(
            existing?.copy(
                note = normalizedNote,
                updatedAtMillis = clock(),
            ) ?: TodayEntryEntity(
                routineItemId = routineItemId,
                date = date,
                note = normalizedNote,
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun updateCompletedCount(
        date: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        val existing = todayEntryDao.entryForDate(date, routineItemId).first()
        todayEntryDao.upsert(
            existing?.copy(
                completedCount = completedCount.coerceAtLeast(0),
                updatedAtMillis = clock(),
            ) ?: TodayEntryEntity(
                routineItemId = routineItemId,
                date = date,
                completedCount = completedCount.coerceAtLeast(0),
                updatedAtMillis = clock(),
            ),
        )
    }

    override suspend fun resetDate(date: String) {
        todayEntryDao.deleteEntriesForDate(date)
    }
}

private fun RoutineItemWithAction.toTodayDomain(
    date: String,
    todayEntry: TodayEntryEntity?,
): TodayRoutineItem {
    val completedCount = action.repeatTargetCount?.let { targetCount ->
        (todayEntry?.completedCount ?: 0).coerceIn(0, targetCount)
    } ?: 0
    return TodayRoutineItem(
        routineItemId = routineItem.id,
        actionId = action.id,
        title = action.title,
        description = action.description,
        repeatTargetCount = action.repeatTargetCount,
        completedCount = completedCount,
        position = routineItem.position,
        date = date,
        isChecked = action.repeatTargetCount?.let { targetCount ->
            completedCount >= targetCount
        } ?: (todayEntry?.isChecked ?: false),
        note = todayEntry?.note,
    )
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> "DAILY"
        RoutineCadence.Weekly -> "WEEKLY"
    }
