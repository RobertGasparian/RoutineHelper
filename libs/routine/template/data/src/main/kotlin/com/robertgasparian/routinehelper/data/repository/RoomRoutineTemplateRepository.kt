package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRoutineTemplateRepository @Inject constructor(
    private val database: RoomDatabase,
    private val actionDao: ActionDao,
    private val routineItemDao: RoutineItemDao,
    private val timeProvider: TimeProvider,
) : RoutineTemplateRepository {
    override fun templateItems(cadence: RoutineCadence): Flow<List<RoutineTemplateItem>> =
        routineItemDao.routineItems(cadence.toStorageValue()).map { items ->
            items.map(RoutineItemWithAction::toDomain)
        }

    override fun templateItem(actionId: Long): Flow<RoutineTemplateItem?> =
        combine(
            templateItems(RoutineCadence.Daily),
            templateItems(RoutineCadence.Weekly),
        ) { dailyItems, weeklyItems ->
            (dailyItems + weeklyItems).firstOrNull { it.actionId == actionId }
        }

    override suspend fun addTemplateItem(
        title: String,
        description: String?,
        repeatTargetCount: Int?,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val now = timeProvider.currentTimeMillis()
        val actionId = actionDao.insert(
            ActionEntity(
                title = title.trim(),
                description = description?.trim()?.takeIf(String::isNotEmpty),
                repeatTargetCount = repeatTargetCount,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
        routineItemDao.insert(
            RoutineItemEntity(
                actionId = actionId,
                position = routineItemDao.maxPosition(cadence.toStorageValue()).first() + 1,
                cadence = cadence.toStorageValue(),
                createdAtMillis = now,
            ),
        )
    }

    override suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
        repeatTargetCount: Int?,
    ) {
        val existing = actionDao.action(actionId).first() ?: return
        actionDao.update(
            existing.copy(
                title = title.trim(),
                description = description?.trim()?.takeIf(String::isNotEmpty),
                repeatTargetCount = repeatTargetCount,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
        )
    }

    override suspend fun removeTemplateItem(routineItemId: Long) {
        database.withTransaction {
            val routineItem = routineItemDao.routineItem(routineItemId).first() ?: return@withTransaction
            routineItemDao.deleteById(routineItemId)
            actionDao.deleteById(routineItem.actionId)
        }
    }

    override suspend fun reorderTemplateItems(routineItemIdsInOrder: List<Long>) {
        database.withTransaction {
            routineItemIdsInOrder.forEachIndexed { index, routineItemId ->
                val routineItem = routineItemDao.routineItem(routineItemId).first()
                if (routineItem != null && routineItem.position != index) {
                    routineItemDao.update(routineItem.copy(position = index))
                }
            }
        }
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

private fun RoutineItemWithAction.toDomain(): RoutineTemplateItem =
    RoutineTemplateItem(
        routineItemId = routineItem.id,
        actionId = action.id,
        title = action.title,
        description = action.description,
        repeatTargetCount = action.repeatTargetCount,
        position = routineItem.position,
        cadence = routineItem.cadence.toRoutineCadence(),
    )
