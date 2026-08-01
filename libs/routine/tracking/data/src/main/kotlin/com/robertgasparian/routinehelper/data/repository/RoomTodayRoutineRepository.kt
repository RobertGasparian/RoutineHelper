package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.DailyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.DailyReflectionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.DailyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RoomTodayRoutineRepository @Inject constructor(
    private val database: RoomDatabase,
    private val routineItemDao: RoutineItemDao,
    private val dailyEntryDao: DailyEntryDao,
    private val dailyReflectionDao: DailyReflectionDao,
    private val timeProvider: TimeProvider,
) : TodayRoutineRepository {
    override fun todayItems(date: String): Flow<List<TodayRoutineItem>> =
        combine(
            routineItemDao.routineItems(RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE),
            dailyEntryDao.entriesForDate(date),
        ) { routineItems, dailyEntries ->
            val entriesByRoutineItemId = dailyEntries.associateBy { it.routineItemId }
            routineItems.map { routineItem ->
                routineItem.toTodayDomain(
                    date = date,
                    dailyEntry = entriesByRoutineItemId[routineItem.routineItem.id],
                )
            }
        }

    override fun reflection(date: String): Flow<RoutineReflection> =
        dailyReflectionDao.reflectionForDate(date).map { storedReflection ->
            RoutineReflection(
                summaryNote = storedReflection?.summaryNote,
                rating = storedReflection?.rating?.let(::ReflectionRating),
            )
        }

    override suspend fun setChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        dailyEntryDao.upsertChecked(
            date = date,
            routineItemId = routineItemId,
            isChecked = isChecked,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateNote(
        date: String,
        routineItemId: Long,
        note: String?,
    ) {
        val normalizedNote = note?.trim()?.takeIf(String::isNotEmpty)
        dailyEntryDao.upsertNote(
            date = date,
            routineItemId = routineItemId,
            note = normalizedNote,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateCompletedCount(
        date: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        dailyEntryDao.upsertCompletedCount(
            date = date,
            routineItemId = routineItemId,
            completedCount = completedCount.coerceAtLeast(0),
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun setHidden(
        date: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        dailyEntryDao.upsertHidden(
            date = date,
            routineItemId = routineItemId,
            isHidden = isHidden,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateReflection(
        date: String,
        reflection: RoutineReflection,
    ) {
        val normalizedReflection = reflection.normalized()
        if (normalizedReflection.isEmpty) {
            dailyReflectionDao.deleteForDate(date)
        } else {
            dailyReflectionDao.upsert(
                DailyReflectionEntity(
                    date = date,
                    summaryNote = normalizedReflection.summaryNote,
                    rating = normalizedReflection.rating?.value,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun resetDate(date: String) {
        database.withTransaction {
            dailyEntryDao.deleteEntriesForDate(date)
            dailyReflectionDao.deleteForDate(date)
        }
    }
}

private fun RoutineReflection.normalized(): RoutineReflection =
    copy(summaryNote = summaryNote?.trim()?.takeIf(String::isNotEmpty))

private fun RoutineItemWithAction.toTodayDomain(
    date: String,
    dailyEntry: DailyEntryEntity?,
): TodayRoutineItem {
    val completedCount = action.repeatTargetCount?.let { targetCount ->
        (dailyEntry?.completedCount ?: 0).coerceIn(0, targetCount)
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
        } ?: (dailyEntry?.isChecked ?: false),
        isHidden = dailyEntry?.isHidden ?: false,
        note = dailyEntry?.note,
    )
}
