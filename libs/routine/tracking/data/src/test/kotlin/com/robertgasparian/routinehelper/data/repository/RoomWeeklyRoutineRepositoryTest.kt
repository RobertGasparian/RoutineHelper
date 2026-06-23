package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklySummaryNoteDao
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklySummaryNoteEntity
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.test.FixedTimeProvider
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
    private val weeklySummaryNoteDao = FakeWeeklySummaryNoteDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomWeeklyRoutineRepository(
        database = database,
        routineItemDao = routineItemDao,
        weeklyEntryDao = weeklyEntryDao,
        weeklySummaryNoteDao = weeklySummaryNoteDao,
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
    fun `given persisted weekly summary note when observing it then emits its text`() = runTest {
        weeklySummaryNoteDao.notes[WEEK_START_DATE] = WeeklySummaryNoteEntity(
            weekStartDate = WEEK_START_DATE,
            note = "Good progress",
            updatedAtMillis = 1L,
        )

        assertEquals("Good progress", repository.summaryNote(WEEK_START_DATE).first())
    }

    @Test
    fun `given weekly summary text when updating it then trims content and deletes blank content`() = runTest {
        repository.updateSummaryNote(WEEK_START_DATE, "  Good progress  ")

        assertEquals(
            WeeklySummaryNoteEntity(
                weekStartDate = WEEK_START_DATE,
                note = "Good progress",
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            weeklySummaryNoteDao.notes[WEEK_START_DATE],
        )

        repository.updateSummaryNote(WEEK_START_DATE, "   ")

        assertEquals(emptyMap<String, WeeklySummaryNoteEntity>(), weeklySummaryNoteDao.notes)
        assertEquals(listOf(WEEK_START_DATE), weeklySummaryNoteDao.deletedWeekStartDates)
    }

    @Test
    fun `given stored weekly state when resetting the week then deletes entries and summary note`() = runTest {
        weeklyEntryDao.entries[WEEK_START_DATE to 10L] = weeklyEntry(routineItemId = 10L)
        weeklySummaryNoteDao.notes[WEEK_START_DATE] = WeeklySummaryNoteEntity(
            weekStartDate = WEEK_START_DATE,
            note = "Good progress",
            updatedAtMillis = 1L,
        )

        repository.resetWeek(WEEK_START_DATE)

        assertEquals(emptyMap<Pair<String, Long>, WeeklyEntryEntity>(), weeklyEntryDao.entries)
        assertEquals(listOf(WEEK_START_DATE), weeklyEntryDao.deletedWeekStartDates)
        assertEquals(emptyMap<String, WeeklySummaryNoteEntity>(), weeklySummaryNoteDao.notes)
        assertEquals(listOf(WEEK_START_DATE), weeklySummaryNoteDao.deletedWeekStartDates)
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

private class FakeWeeklySummaryNoteDao : WeeklySummaryNoteDao {
    val notes = mutableMapOf<String, WeeklySummaryNoteEntity>()
    val deletedWeekStartDates = mutableListOf<String>()

    override fun noteForWeek(weekStartDate: String): Flow<WeeklySummaryNoteEntity?> =
        flowOf(notes[weekStartDate])

    override suspend fun upsert(note: WeeklySummaryNoteEntity) {
        notes[note.weekStartDate] = note
    }

    override suspend fun deleteForWeek(weekStartDate: String) {
        notes.remove(weekStartDate)
        deletedWeekStartDates += weekStartDate
    }
}
