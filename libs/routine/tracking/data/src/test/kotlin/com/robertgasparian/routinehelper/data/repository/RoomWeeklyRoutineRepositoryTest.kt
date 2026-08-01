package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyReflectionDao
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomWeeklyRoutineRepositoryTest {
    private val database = TrackingTestRoomDatabase()
    private val routineItemDao = FakeRoutineItemDao()
    private val weeklyEntryDao = FakeWeeklyEntryDao()
    private val weeklyReflectionDao = FakeWeeklyReflectionDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomWeeklyRoutineRepository(
        database = database,
        routineItemDao = routineItemDao,
        weeklyEntryDao = weeklyEntryDao,
        weeklyReflectionDao = weeklyReflectionDao,
        timeProvider = timeProvider,
    )

    @Test
    fun `given routine items and entries when observing the week then maps and clamps their state`() = runTest {
        routineItemDao.items = listOf(
            routineItemFixture(
                routineItemId = 10L,
                actionId = 100L,
                title = "Exercise",
                position = 0,
                repeatTargetCount = 3,
                cadence = "WEEKLY",
            ),
            routineItemFixture(
                routineItemId = 20L,
                actionId = 200L,
                title = "Plan meals",
                position = 1,
                cadence = "WEEKLY",
            ),
            routineItemFixture(
                routineItemId = 30L,
                actionId = 300L,
                title = "Call family",
                position = 2,
                cadence = "WEEKLY",
            ),
        )
        weeklyEntryDao.entries[WEEK_START_DATE to 10L] = weeklyEntry(
            routineItemId = 10L,
            completedCount = 5,
            isHidden = true,
            note = "Three sessions done",
        )
        weeklyEntryDao.entries[WEEK_START_DATE to 20L] = weeklyEntry(
            routineItemId = 20L,
            isChecked = true,
            completedCount = 4,
        )

        val items = repository.weeklyItems(WEEK_START_DATE).first()

        assertEquals("WEEKLY", routineItemDao.requestedCadence)
        assertEquals(
            listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Exercise",
                    description = null,
                    position = 0,
                    weekStartDate = WEEK_START_DATE,
                    isChecked = true,
                    isHidden = true,
                    note = "Three sessions done",
                    repeatTargetCount = 3,
                    completedCount = 3,
                ),
                WeeklyRoutineItem(
                    routineItemId = 20L,
                    actionId = 200L,
                    title = "Plan meals",
                    description = null,
                    position = 1,
                    weekStartDate = WEEK_START_DATE,
                    isChecked = true,
                    note = null,
                ),
                WeeklyRoutineItem(
                    routineItemId = 30L,
                    actionId = 300L,
                    title = "Call family",
                    description = null,
                    position = 2,
                    weekStartDate = WEEK_START_DATE,
                    isChecked = false,
                    note = null,
                ),
            ),
            items,
        )
    }

    @Test
    fun `given no entry when checking a weekly item then creates an entry for the week`() = runTest {
        repository.setChecked(
            weekStartDate = WEEK_START_DATE,
            routineItemId = 10L,
            isChecked = true,
        )

        assertEquals(
            weeklyEntry(
                routineItemId = 10L,
                isChecked = true,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            weeklyEntryDao.entries.getValue(WEEK_START_DATE to 10L),
        )
    }

    @Test
    fun `given an existing weekly entry when updating its fields then normalizes values and preserves unrelated state`() = runTest {
        weeklyEntryDao.entries[WEEK_START_DATE to 10L] = weeklyEntry(
            id = 7L,
            routineItemId = 10L,
            isChecked = true,
            completedCount = 2,
            isHidden = true,
            note = "Old note",
            updatedAtMillis = 1L,
        )

        repository.setChecked(WEEK_START_DATE, 10L, false)
        repository.updateNote(WEEK_START_DATE, 10L, "  New note  ")
        repository.updateCompletedCount(WEEK_START_DATE, 10L, -4)
        repository.setHidden(WEEK_START_DATE, 10L, false)

        assertEquals(
            weeklyEntry(
                id = 7L,
                routineItemId = 10L,
                isChecked = false,
                completedCount = 0,
                isHidden = false,
                note = "New note",
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            weeklyEntryDao.entries.getValue(WEEK_START_DATE to 10L),
        )
    }

    @Test
    fun `given persisted weekly reflection when observing it then emits text and rating`() = runTest {
        weeklyReflectionDao.reflections[WEEK_START_DATE] = WeeklyReflectionEntity(
            weekStartDate = WEEK_START_DATE,
            summaryNote = "Good progress",
            rating = 5,
            updatedAtMillis = 1L,
        )

        assertEquals(
            RoutineReflection(summaryNote = "Good progress", rating = ReflectionRating(5)),
            repository.reflection(WEEK_START_DATE).first(),
        )
    }

    @Test
    fun `given weekly reflection when updating it then trims text and deletes an empty reflection`() = runTest {
        repository.updateReflection(
            WEEK_START_DATE,
            RoutineReflection(summaryNote = "  Good progress  ", rating = ReflectionRating(5)),
        )

        assertEquals(
            WeeklyReflectionEntity(
                weekStartDate = WEEK_START_DATE,
                summaryNote = "Good progress",
                rating = 5,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            weeklyReflectionDao.reflections[WEEK_START_DATE],
        )

        repository.updateReflection(WEEK_START_DATE, RoutineReflection(summaryNote = "   "))

        assertEquals(emptyMap<String, WeeklyReflectionEntity>(), weeklyReflectionDao.reflections)
        assertEquals(listOf(WEEK_START_DATE), weeklyReflectionDao.deletedWeekStartDates)
    }

    @Test
    fun `given rating without text when updating weekly reflection then rating remains persisted`() = runTest {
        repository.updateReflection(
            WEEK_START_DATE,
            RoutineReflection(rating = ReflectionRating(2)),
        )

        assertEquals(
            WeeklyReflectionEntity(
                weekStartDate = WEEK_START_DATE,
                summaryNote = null,
                rating = 2,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            weeklyReflectionDao.reflections[WEEK_START_DATE],
        )
        assertEquals(emptyList<String>(), weeklyReflectionDao.deletedWeekStartDates)
    }

    @Test
    fun `given stored weekly state when resetting the week then deletes entries and reflection`() = runTest {
        weeklyEntryDao.entries[WEEK_START_DATE to 10L] = weeklyEntry(routineItemId = 10L)
        weeklyReflectionDao.reflections[WEEK_START_DATE] = WeeklyReflectionEntity(
            weekStartDate = WEEK_START_DATE,
            summaryNote = "Good progress",
            rating = null,
            updatedAtMillis = 1L,
        )

        repository.resetWeek(WEEK_START_DATE)

        assertEquals(emptyMap<Pair<String, Long>, WeeklyEntryEntity>(), weeklyEntryDao.entries)
        assertEquals(listOf(WEEK_START_DATE), weeklyEntryDao.deletedWeekStartDates)
        assertEquals(emptyMap<String, WeeklyReflectionEntity>(), weeklyReflectionDao.reflections)
        assertEquals(listOf(WEEK_START_DATE), weeklyReflectionDao.deletedWeekStartDates)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    private fun weeklyEntry(
        id: Long = 0L,
        routineItemId: Long,
        isChecked: Boolean = false,
        completedCount: Int = 0,
        isHidden: Boolean = false,
        note: String? = null,
        updatedAtMillis: Long = 1L,
    ): WeeklyEntryEntity =
        WeeklyEntryEntity(
            id = id,
            routineItemId = routineItemId,
            weekStartDate = WEEK_START_DATE,
            isChecked = isChecked,
            completedCount = completedCount,
            isHidden = isHidden,
            note = note,
            updatedAtMillis = updatedAtMillis,
        )

    private companion object {
        const val WEEK_START_DATE = "2026-05-25"
    }
}

private class FakeWeeklyEntryDao : WeeklyEntryDao {
    val entries = mutableMapOf<Pair<String, Long>, WeeklyEntryEntity>()
    val deletedWeekStartDates = mutableListOf<String>()

    override fun entriesForWeek(weekStartDate: String): Flow<List<WeeklyEntryEntity>> =
        flowOf(entries.filterKeys { (entryWeek, _) -> entryWeek == weekStartDate }.values.toList())

    override suspend fun upsertChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
        updatedAtMillis: Long,
    ) {
        updateEntry(weekStartDate, routineItemId, updatedAtMillis) { entry ->
            entry.copy(isChecked = isChecked)
        }
    }

    override suspend fun upsertNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
        updatedAtMillis: Long,
    ) {
        updateEntry(weekStartDate, routineItemId, updatedAtMillis) { entry ->
            entry.copy(note = note)
        }
    }

    override suspend fun upsertCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
        updatedAtMillis: Long,
    ) {
        updateEntry(weekStartDate, routineItemId, updatedAtMillis) { entry ->
            entry.copy(completedCount = completedCount)
        }
    }

    override suspend fun upsertHidden(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
        updatedAtMillis: Long,
    ) {
        updateEntry(weekStartDate, routineItemId, updatedAtMillis) { entry ->
            entry.copy(isHidden = isHidden)
        }
    }

    override suspend fun deleteEntriesForWeek(weekStartDate: String) {
        entries.keys.removeAll { (entryWeek, _) -> entryWeek == weekStartDate }
        deletedWeekStartDates += weekStartDate
    }

    private fun updateEntry(
        weekStartDate: String,
        routineItemId: Long,
        updatedAtMillis: Long,
        update: (WeeklyEntryEntity) -> WeeklyEntryEntity,
    ) {
        val key = weekStartDate to routineItemId
        val existing = entries[key] ?: WeeklyEntryEntity(
            routineItemId = routineItemId,
            weekStartDate = weekStartDate,
            updatedAtMillis = updatedAtMillis,
        )
        entries[key] = update(existing).copy(updatedAtMillis = updatedAtMillis)
    }
}

private class FakeWeeklyReflectionDao : WeeklyReflectionDao {
    val reflections = mutableMapOf<String, WeeklyReflectionEntity>()
    val deletedWeekStartDates = mutableListOf<String>()

    override fun reflectionForWeek(weekStartDate: String): Flow<WeeklyReflectionEntity?> =
        flowOf(reflections[weekStartDate])

    override suspend fun upsert(reflection: WeeklyReflectionEntity) {
        reflections[reflection.weekStartDate] = reflection
    }

    override suspend fun deleteForWeek(weekStartDate: String) {
        reflections.remove(weekStartDate)
        deletedWeekStartDates += weekStartDate
    }
}
