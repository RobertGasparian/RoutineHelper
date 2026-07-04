package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineItemWithAction
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomRoutineTemplateRepositoryTest {
    private val database = TestRoomDatabase()
    private val actionDao = FakeActionDao()
    private val routineItemDao = FakeRoutineItemDao(actionDao)
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomRoutineTemplateRepository(
        database = database,
        actionDao = actionDao,
        routineItemDao = routineItemDao,
        timeProvider = timeProvider,
    )

    @Test
    fun `given stored template items when observing a cadence then maps only matching items`() = runTest {
        storeTemplateItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            description = "Three bottles",
            repeatTargetCount = 3,
            position = 1,
            cadence = "DAILY",
        )
        storeTemplateItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Plan meals",
            position = 0,
            cadence = "WEEKLY",
        )

        val items = repository.templateItems(RoutineCadence.Daily).first()

        assertEquals(
            listOf(
                RoutineTemplateItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = "Three bottles",
                    repeatTargetCount = 3,
                    position = 1,
                    cadence = RoutineCadence.Daily,
                ),
            ),
            items,
        )
        assertEquals(listOf("DAILY"), routineItemDao.requestedCadences)
    }

    @Test
    fun `given action in weekly template when finding by action id then returns weekly item`() = runTest {
        storeTemplateItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Plan meals",
            position = 0,
            cadence = "WEEKLY",
        )

        val item = repository.templateItem(actionId = 200L).first()

        assertEquals(
            RoutineTemplateItem(
                routineItemId = 20L,
                actionId = 200L,
                title = "Plan meals",
                description = null,
                position = 0,
                cadence = RoutineCadence.Weekly,
            ),
            item,
        )
        assertEquals(listOf("DAILY", "WEEKLY"), routineItemDao.requestedCadences)
    }

    @Test
    fun `given new template values when adding item then normalizes and appends within cadence`() = runTest {
        storeTemplateItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Existing",
            position = 2,
            cadence = "DAILY",
        )

        val routineItemId = repository.addTemplateItem(
            title = "  Drink water  ",
            description = "   ",
            repeatTargetCount = 3,
            cadence = RoutineCadence.Daily,
        )

        val routineItem = routineItemDao.items.getValue(routineItemId)
        val action = actionDao.items.getValue(routineItem.actionId)
        assertEquals("Drink water", action.title)
        assertEquals(null, action.description)
        assertEquals(3, action.repeatTargetCount)
        assertEquals(timeProvider.currentTimeMillis(), action.createdAtMillis)
        assertEquals(timeProvider.currentTimeMillis(), action.updatedAtMillis)
        assertEquals(3, routineItem.position)
        assertEquals("DAILY", routineItem.cadence)
        assertEquals(timeProvider.currentTimeMillis(), routineItem.createdAtMillis)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    @Test
    fun `given existing action when updating it then normalizes fields and preserves identity`() = runTest {
        actionDao.items[100L] = ActionEntity(
            id = 100L,
            title = "Old title",
            description = "Old description",
            repeatTargetCount = null,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        )

        repository.updateAction(
            actionId = 100L,
            title = "  New title  ",
            description = "  New description  ",
            repeatTargetCount = 4,
        )

        assertEquals(
            ActionEntity(
                id = 100L,
                title = "New title",
                description = "New description",
                repeatTargetCount = 4,
                createdAtMillis = 1L,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            actionDao.items[100L],
        )
    }

    @Test
    fun `given template item when removing it then deletes routine item and action`() = runTest {
        storeTemplateItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            position = 0,
            cadence = "DAILY",
        )

        repository.removeTemplateItem(routineItemId = 10L)

        assertEquals(emptyMap<Long, RoutineItemEntity>(), routineItemDao.items)
        assertEquals(emptyMap<Long, ActionEntity>(), actionDao.items)
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `given template order when reordering then updates positions and ignores unknown ids`() = runTest {
        storeTemplateItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "First",
            position = 0,
            cadence = RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE,
        )
        storeTemplateItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Second",
            position = 1,
            cadence = RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE,
        )
        storeTemplateItem(
            routineItemId = 30L,
            actionId = 300L,
            title = "Third",
            position = 2,
            cadence = RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE,
        )

        repository.reorderTemplateItems(
            cadence = RoutineCadence.Daily,
            routineItemIdsInOrder = listOf(30L, 999L, 10L, 20L),
        )

        assertEquals(1, routineItemDao.items.getValue(10L).position)
        assertEquals(2, routineItemDao.items.getValue(20L).position)
        assertEquals(0, routineItemDao.items.getValue(30L).position)
        assertEquals(listOf(30L, 10L, 20L), routineItemDao.updatedIds)
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `given mixed cadence item ids when reordering daily then updates only daily positions`() = runTest {
        storeTemplateItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Daily first",
            position = 0,
            cadence = RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE,
        )
        storeTemplateItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Daily second",
            position = 1,
            cadence = RoutineItemEntity.DAILY_CADENCE_STORAGE_VALUE,
        )
        storeTemplateItem(
            routineItemId = 30L,
            actionId = 300L,
            title = "Weekly first",
            position = 0,
            cadence = RoutineItemEntity.WEEKLY_CADENCE_STORAGE_VALUE,
        )

        repository.reorderTemplateItems(
            cadence = RoutineCadence.Daily,
            routineItemIdsInOrder = listOf(30L, 20L, 10L),
        )

        assertEquals(1, routineItemDao.items.getValue(10L).position)
        assertEquals(0, routineItemDao.items.getValue(20L).position)
        assertEquals(0, routineItemDao.items.getValue(30L).position)
        assertEquals(listOf(20L, 10L), routineItemDao.updatedIds)
        assertEquals(1, database.transactionSuccesses)
    }

    private fun storeTemplateItem(
        routineItemId: Long,
        actionId: Long,
        title: String,
        position: Int,
        cadence: String,
        description: String? = null,
        repeatTargetCount: Int? = null,
    ) {
        actionDao.items[actionId] = ActionEntity(
            id = actionId,
            title = title,
            description = description,
            repeatTargetCount = repeatTargetCount,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        )
        routineItemDao.items[routineItemId] = RoutineItemEntity(
            id = routineItemId,
            actionId = actionId,
            position = position,
            cadence = cadence,
            createdAtMillis = 1L,
        )
    }
}

private class FakeActionDao : ActionDao {
    val items = mutableMapOf<Long, ActionEntity>()
    private var nextId = 1_000L

    override fun actions(): Flow<List<ActionEntity>> = flowOf(items.values.toList())

    override fun action(id: Long): Flow<ActionEntity?> = flowOf(items[id])

    override suspend fun insert(action: ActionEntity): Long {
        val id = action.id.takeIf { it != 0L } ?: nextId++
        items[id] = action.copy(id = id)
        return id
    }

    override suspend fun update(action: ActionEntity) {
        items[action.id] = action
    }

    override suspend fun deleteById(id: Long) {
        items.remove(id)
    }
}

private class FakeRoutineItemDao(
    private val actionDao: FakeActionDao,
) : RoutineItemDao {
    val items = mutableMapOf<Long, RoutineItemEntity>()
    val requestedCadences = mutableListOf<String>()
    val updatedIds = mutableListOf<Long>()
    private var nextId = 2_000L

    override fun routineItems(cadence: String): Flow<List<RoutineItemWithAction>> {
        requestedCadences += cadence
        return flowOf(
            items.values
                .filter { item -> item.cadence == cadence }
                .sortedBy(RoutineItemEntity::position)
                .map { item ->
                    RoutineItemWithAction(
                        routineItem = item,
                        action = actionDao.items.getValue(item.actionId),
                    )
                },
        )
    }

    override fun maxPosition(cadence: String): Flow<Int> =
        flowOf(
            items.values
                .filter { item -> item.cadence == cadence }
                .maxOfOrNull(RoutineItemEntity::position) ?: -1,
        )

    override fun routineItem(id: Long): Flow<RoutineItemEntity?> = flowOf(items[id])

    override suspend fun insert(routineItem: RoutineItemEntity): Long {
        val id = routineItem.id.takeIf { it != 0L } ?: nextId++
        items[id] = routineItem.copy(id = id)
        return id
    }

    override suspend fun update(routineItem: RoutineItemEntity) {
        items[routineItem.id] = routineItem
        updatedIds += routineItem.id
    }

    override suspend fun deleteById(id: Long) {
        items.remove(id)
    }
}

private class TestRoomDatabase : RoomDatabase() {
    var transactionBegins = 0
        private set
    var transactionSuccesses = 0
        private set
    var transactionEnds = 0
        private set

    override val transactionExecutor: Executor = Executor(Runnable::run)

    override fun createInvalidationTracker(): InvalidationTracker =
        error("Invalidation tracking is not used by repository unit tests")

    override fun clearAllTables() = Unit

    override fun beginTransaction() {
        transactionBegins += 1
    }

    override fun setTransactionSuccessful() {
        transactionSuccesses += 1
    }

    override fun endTransaction() {
        transactionEnds += 1
    }
}
