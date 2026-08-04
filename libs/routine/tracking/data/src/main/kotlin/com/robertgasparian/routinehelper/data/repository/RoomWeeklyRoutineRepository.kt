package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.ReflectionTagDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyReflectionDao
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionTagSelectionEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RoomWeeklyRoutineRepository @Inject constructor(
    private val database: RoomDatabase,
    private val routineItemDao: RoutineItemDao,
    private val weeklyEntryDao: WeeklyEntryDao,
    private val weeklyReflectionDao: WeeklyReflectionDao,
    private val reflectionTagDao: ReflectionTagDao,
    private val timeProvider: TimeProvider,
) : WeeklyRoutineRepository {
    override fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>> =
        combine(
            routineItemDao.routineItems(RoutineItemEntity.WEEKLY_CADENCE_STORAGE_VALUE),
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

    override fun reflection(weekStartDate: String): Flow<RoutineReflection> =
        weeklyReflectionDao.reflectionForWeek(weekStartDate).map { storedReflection ->
            RoutineReflection(
                summaryNote = storedReflection?.reflection?.summaryNote,
                rating = storedReflection?.reflection?.rating?.let(::ReflectionRating),
                selectedTags = storedReflection?.tags.orEmpty()
                    .sortedWith(compareBy(ReflectionTagEntity::position, ReflectionTagEntity::id))
                    .map(ReflectionTagEntity::toWeeklySelectedDomain),
            )
        }

    override suspend fun setChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        weeklyEntryDao.upsertChecked(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isChecked = isChecked,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
    ) {
        val normalizedNote = note?.trim()?.takeIf(String::isNotEmpty)
        weeklyEntryDao.upsertNote(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            note = normalizedNote,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        weeklyEntryDao.upsertCompletedCount(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            completedCount = completedCount.coerceAtLeast(0),
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun setHidden(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        weeklyEntryDao.upsertHidden(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isHidden = isHidden,
            updatedAtMillis = timeProvider.currentTimeMillis(),
        )
    }

    override suspend fun updateReflection(
        weekStartDate: String,
        reflection: RoutineReflection,
    ) = database.withTransaction {
        val normalizedReflection = normalizeReflectionWithStoredTags(reflection)
        if (normalizedReflection.isEmpty) {
            weeklyReflectionDao.deleteForWeek(weekStartDate)
        } else {
            weeklyReflectionDao.upsert(
                WeeklyReflectionEntity(
                    weekStartDate = weekStartDate,
                    summaryNote = normalizedReflection.summaryNote,
                    rating = normalizedReflection.rating?.value,
                    updatedAtMillis = timeProvider.currentTimeMillis(),
                ),
            )
            weeklyReflectionDao.deleteTagSelectionsForWeek(weekStartDate)
            val selections = normalizedReflection.selectedTags.map { tag ->
                WeeklyReflectionTagSelectionEntity(
                    weekStartDate = weekStartDate,
                    tagId = requireNotNull(tag.templateTagId),
                )
            }
            if (selections.isNotEmpty()) weeklyReflectionDao.insertTagSelections(selections)
        }
    }

    override suspend fun resetWeek(weekStartDate: String) {
        database.withTransaction {
            weeklyEntryDao.deleteEntriesForWeek(weekStartDate)
            weeklyReflectionDao.deleteForWeek(weekStartDate)
        }
    }

    private suspend fun normalizeReflectionWithStoredTags(
        reflection: RoutineReflection,
    ): RoutineReflection {
        val tagIds = reflection.selectedTags.map { tag ->
            requireNotNull(tag.templateTagId) {
                "Current reflections can select only cadence-template tags"
            }
        }
        require(tagIds.distinct().size == tagIds.size) {
            "Current reflection tags must be unique"
        }
        val storedTags = if (tagIds.isEmpty()) {
            emptyList()
        } else {
            reflectionTagDao.tagsByIds(
                cadence = ReflectionTagEntity.WEEKLY_CADENCE_STORAGE_VALUE,
                tagIds = tagIds,
            )
        }
        require(storedTags.size == tagIds.size) {
            "Current reflection contains a tag from a different cadence or a deleted tag"
        }
        return reflection.copy(
            summaryNote = reflection.summaryNote?.trim()?.takeIf(String::isNotEmpty),
            selectedTags = storedTags
                .sortedWith(compareBy(ReflectionTagEntity::position, ReflectionTagEntity::id))
                .map(ReflectionTagEntity::toWeeklySelectedDomain),
        )
    }
}

private fun ReflectionTagEntity.toWeeklySelectedDomain(): SelectedReflectionTag =
    SelectedReflectionTag(
        label = label,
        position = position,
        templateTagId = id,
    )

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
        isHidden = weeklyEntry?.isHidden ?: false,
        note = weeklyEntry?.note,
    )
}
