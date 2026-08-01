package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.data.local.dao.DailyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.DailyReflectionDao
import com.robertgasparian.routinehelper.data.local.entity.DailyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomTodayRoutineRepositoryTest {
    private val database = TrackingTestRoomDatabase()
    private val routineItemDao = FakeRoutineItemDao()
    private val dailyEntryDao = FakeDailyEntryDao()
    private val dailyReflectionDao = FakeDailyReflectionDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomTodayRoutineRepository(
        database = database,
        routineItemDao = routineItemDao,
        dailyEntryDao = dailyEntryDao,
        dailyReflectionDao = dailyReflectionDao,
        timeProvider = timeProvider,
    )

    @Test
    fun `given routine items and entries when observing today then maps and clamps their state`() = runTest {
        routineItemDao.items = listOf(
            routineItemFixture(
                routineItemId = 10L,
                actionId = 100L,
                title = "Drink water",
                position = 0,
                repeatTargetCount = 3,
            ),
            routineItemFixture(
                routineItemId = 20L,
                actionId = 200L,
                title = "Read",
                position = 1,
            ),
            routineItemFixture(
                routineItemId = 30L,
                actionId = 300L,
                title = "Stretch",
                position = 2,
            ),
        )
        dailyEntryDao.entries[DATE to 10L] = dailyEntry(
            routineItemId = 10L,
            completedCount = 5,
            isHidden = true,
            note = "Finished early",
        )
        dailyEntryDao.entries[DATE to 20L] = dailyEntry(
            routineItemId = 20L,
            isChecked = true,
            completedCount = 4,
        )

        val items = repository.todayItems(DATE).first()

        assertEquals("DAILY", routineItemDao.requestedCadence)
        assertEquals(
            listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = DATE,
                    isChecked = true,
                    isHidden = true,
                    note = "Finished early",
                    repeatTargetCount = 3,
                    completedCount = 3,
                ),
                TodayRoutineItem(
                    routineItemId = 20L,
                    actionId = 200L,
                    title = "Read",
                    description = null,
                    position = 1,
                    date = DATE,
                    isChecked = true,
                    note = null,
                ),
                TodayRoutineItem(
                    routineItemId = 30L,
                    actionId = 300L,
                    title = "Stretch",
                    description = null,
                    position = 2,
                    date = DATE,
                    isChecked = false,
                    note = null,
                ),
            ),
            items,
        )
    }

    @Test
    fun `given no entry when checking an item then creates a dated entry with the current timestamp`() = runTest {
        repository.setChecked(
            date = DATE,
            routineItemId = 10L,
            isChecked = true,
        )

        assertEquals(
            dailyEntry(
                routineItemId = 10L,
                isChecked = true,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            dailyEntryDao.entries.getValue(DATE to 10L),
        )
    }

    @Test
    fun `given an existing entry when updating its fields then normalizes values and preserves unrelated state`() = runTest {
        dailyEntryDao.entries[DATE to 10L] = dailyEntry(
            id = 7L,
            routineItemId = 10L,
            isChecked = true,
            completedCount = 2,
            isHidden = true,
            note = "Old note",
            updatedAtMillis = 1L,
        )

        repository.setChecked(DATE, 10L, false)
        repository.updateNote(DATE, 10L, "  New note  ")
        repository.updateCompletedCount(DATE, 10L, -4)
        repository.setHidden(DATE, 10L, false)

        assertEquals(
            dailyEntry(
                id = 7L,
                routineItemId = 10L,
                isChecked = false,
                completedCount = 0,
                isHidden = false,
                note = "New note",
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            dailyEntryDao.entries.getValue(DATE to 10L),
        )
    }

    @Test
    fun `given persisted reflection when observing it then emits text and rating`() = runTest {
        dailyReflectionDao.reflections[DATE] = DailyReflectionEntity(
            date = DATE,
            summaryNote = "Steady day",
            rating = 4,
            updatedAtMillis = 1L,
        )

        assertEquals(
            RoutineReflection(summaryNote = "Steady day", rating = ReflectionRating(4)),
            repository.reflection(DATE).first(),
        )
    }

    @Test
    fun `given reflection when updating it then trims text and deletes an empty reflection`() = runTest {
        repository.updateReflection(
            DATE,
            RoutineReflection(summaryNote = "  Steady day  ", rating = ReflectionRating(4)),
        )

        assertEquals(
            DailyReflectionEntity(
                date = DATE,
                summaryNote = "Steady day",
                rating = 4,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            dailyReflectionDao.reflections[DATE],
        )

        repository.updateReflection(DATE, RoutineReflection(summaryNote = "   "))

        assertEquals(emptyMap<String, DailyReflectionEntity>(), dailyReflectionDao.reflections)
        assertEquals(listOf(DATE), dailyReflectionDao.deletedDates)
    }

    @Test
    fun `given rating without text when updating reflection then rating remains persisted`() = runTest {
        repository.updateReflection(
            DATE,
            RoutineReflection(rating = ReflectionRating(3)),
        )

        assertEquals(
            DailyReflectionEntity(
                date = DATE,
                summaryNote = null,
                rating = 3,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            dailyReflectionDao.reflections[DATE],
        )
        assertEquals(emptyList<String>(), dailyReflectionDao.deletedDates)
    }

    @Test
    fun `given stored daily state when resetting the date then deletes entries and reflection`() = runTest {
        dailyEntryDao.entries[DATE to 10L] = dailyEntry(routineItemId = 10L)
        dailyReflectionDao.reflections[DATE] = DailyReflectionEntity(
            date = DATE,
            summaryNote = "Steady day",
            rating = null,
            updatedAtMillis = 1L,
        )

        repository.resetDate(DATE)

        assertEquals(emptyMap<Pair<String, Long>, DailyEntryEntity>(), dailyEntryDao.entries)
        assertEquals(listOf(DATE), dailyEntryDao.deletedDates)
        assertEquals(emptyMap<String, DailyReflectionEntity>(), dailyReflectionDao.reflections)
        assertEquals(listOf(DATE), dailyReflectionDao.deletedDates)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    private fun dailyEntry(
        id: Long = 0L,
        routineItemId: Long,
        isChecked: Boolean = false,
        completedCount: Int = 0,
        isHidden: Boolean = false,
        note: String? = null,
        updatedAtMillis: Long = 1L,
    ): DailyEntryEntity =
        DailyEntryEntity(
            id = id,
            routineItemId = routineItemId,
            date = DATE,
            isChecked = isChecked,
            completedCount = completedCount,
            isHidden = isHidden,
            note = note,
            updatedAtMillis = updatedAtMillis,
        )

    private companion object {
        const val DATE = "2026-05-29"
    }
}

private class FakeDailyEntryDao : DailyEntryDao {
    val entries = mutableMapOf<Pair<String, Long>, DailyEntryEntity>()
    val deletedDates = mutableListOf<String>()

    override fun entriesForDate(date: String): Flow<List<DailyEntryEntity>> =
        flowOf(entries.filterKeys { (entryDate, _) -> entryDate == date }.values.toList())

    override suspend fun upsertChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    ) {
        updateEntry(date, routineItemId, updatedAtMillis) { entry ->
            entry.copy(isChecked = isChecked)
        }
    }

    override suspend fun upsertNote(
        date: String,
        routineItemId: Long,
        note: String?,
        updatedAtMillis: Long,
    ) {
        updateEntry(date, routineItemId, updatedAtMillis) { entry ->
            entry.copy(note = note)
        }
    }

    override suspend fun upsertCompletedCount(
        date: String,
        routineItemId: Long,
        completedCount: Int,
        updatedAtMillis: Long,
    ) {
        updateEntry(date, routineItemId, updatedAtMillis) { entry ->
            entry.copy(completedCount = completedCount)
        }
    }

    override suspend fun upsertHidden(
        date: String,
        routineItemId: Long,
        isHidden: Boolean,
        updatedAtMillis: Long,
    ) {
        updateEntry(date, routineItemId, updatedAtMillis) { entry ->
            entry.copy(isHidden = isHidden)
        }
    }

    override suspend fun deleteEntriesForDate(date: String) {
        entries.keys.removeAll { (entryDate, _) -> entryDate == date }
        deletedDates += date
    }

    private fun updateEntry(
        date: String,
        routineItemId: Long,
        updatedAtMillis: Long,
        update: (DailyEntryEntity) -> DailyEntryEntity,
    ) {
        val key = date to routineItemId
        val existing = entries[key] ?: DailyEntryEntity(
            routineItemId = routineItemId,
            date = date,
            updatedAtMillis = updatedAtMillis,
        )
        entries[key] = update(existing).copy(updatedAtMillis = updatedAtMillis)
    }
}

private class FakeDailyReflectionDao : DailyReflectionDao {
    val reflections = mutableMapOf<String, DailyReflectionEntity>()
    val deletedDates = mutableListOf<String>()

    override fun reflectionForDate(date: String): Flow<DailyReflectionEntity?> =
        flowOf(reflections[date])

    override suspend fun upsert(reflection: DailyReflectionEntity) {
        reflections[reflection.date] = reflection
    }

    override suspend fun deleteForDate(date: String) {
        reflections.remove(date)
        deletedDates += date
    }
}
