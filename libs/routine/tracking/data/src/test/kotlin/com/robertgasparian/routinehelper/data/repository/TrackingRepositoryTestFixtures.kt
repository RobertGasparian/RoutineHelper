package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeRoutineItemDao : RoutineItemDao {
    var items: List<RoutineItemWithAction> = emptyList()
    var requestedCadence: String? = null

    override fun routineItems(cadence: String): Flow<List<RoutineItemWithAction>> {
        requestedCadence = cadence
        return flowOf(items)
    }

    override fun maxPosition(cadence: String): Flow<Int> = flowOf(-1)

    override fun routineItem(id: Long): Flow<RoutineItemEntity?> = flowOf(null)

    override suspend fun insert(routineItem: RoutineItemEntity): Long = routineItem.id

    override suspend fun update(routineItem: RoutineItemEntity) = Unit

    override suspend fun deleteById(id: Long) = Unit
}

internal fun routineItemFixture(
    routineItemId: Long,
    actionId: Long,
    title: String,
    position: Int,
    repeatTargetCount: Int? = null,
    cadence: String = "DAILY",
): RoutineItemWithAction =
    RoutineItemWithAction(
        routineItem = RoutineItemEntity(
            id = routineItemId,
            actionId = actionId,
            position = position,
            cadence = cadence,
            createdAtMillis = 1L,
        ),
        action = ActionEntity(
            id = actionId,
            title = title,
            repeatTargetCount = repeatTargetCount,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        ),
    )
