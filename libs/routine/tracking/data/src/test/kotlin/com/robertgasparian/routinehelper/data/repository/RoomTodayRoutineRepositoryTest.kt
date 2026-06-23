package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.dao.DailySummaryNoteDao
import com.robertgasparian.routinehelper.data.local.dao.TodayEntryDao
import com.robertgasparian.routinehelper.data.local.entity.DailySummaryNoteEntity
import com.robertgasparian.routinehelper.data.local.entity.TodayEntryEntity
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomTodayRoutineRepositoryTest {
    private val database = TrackingTestRoomDatabase()
    private val routineItemDao = FakeRoutineItemDao()
    private val todayEntryDao = FakeTodayEntryDao()
    private val dailySummaryNoteDao = FakeDailySummaryNoteDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomTodayRoutineRepository(
        database = database,
        routineItemDao = routineItemDao,
        todayEntryDao = todayEntryDao,
        dailySummaryNoteDao = dailySummaryNoteDao,
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
        todayEntryDao.entries[DATE to 10L] = todayEntry(
            routineItemId = 10L,
            completedCount = 5,
            isHidden = true,
            note = "Finished early",
        )
        todayEntryDao.entries[DATE to 20L] = todayEntry(
            routineItemId = 20L,
            isChecked = true,
            completedCount = 4,
        )

        val items = repository.todayItems(DATE, RoutineCadence.Daily).first()

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
            todayEntry(
                routineItemId = 10L,
                isChecked = true,
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            todayEntryDao.entries.getValue(DATE to 10L),
        )
    }

    @Test
    fun `given an existing entry when updating its fields then normalizes values and preserves unrelated state`() = runTest {
        todayEntryDao.entries[DATE to 10L] = todayEntry(
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
            todayEntry(
                id = 7L,
                routineItemId = 10L,
                isChecked = false,
                completedCount = 0,
                isHidden = false,
                note = "New note",
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            todayEntryDao.entries.getValue(DATE to 10L),
        )
    }

    @Test
    fun `given persisted summary note when observing it then emits its text`() = runTest {
        dailySummaryNoteDao.notes[DATE] = DailySummaryNoteEntity(
            date = DATE,
            note = "Steady day",
            updatedAtMillis = 1L,
        )

        assertEquals("Steady day", repository.summaryNote(DATE).first())
    }

    @Test
    fun `given summary note text when updating it then trims content and deletes blank content`() = runTest {
        repository.updateSummaryNote(DATE, "  Steady day  ")

        assertEquals(
            DailySummaryNoteEntity(
                date = DATE,
                note = "Steady day",
                updatedAtMillis = timeProvider.currentTimeMillis(),
            ),
            dailySummaryNoteDao.notes[DATE],
        )

        repository.updateSummaryNote(DATE, "   ")

        assertEquals(emptyMap<String, DailySummaryNoteEntity>(), dailySummaryNoteDao.notes)
        assertEquals(listOf(DATE), dailySummaryNoteDao.deletedDates)
    }

    @Test
    fun `given stored daily state when resetting the date then deletes entries and summary note`() = runTest {
        todayEntryDao.entries[DATE to 10L] = todayEntry(routineItemId = 10L)
        dailySummaryNoteDao.notes[DATE] = DailySummaryNoteEntity(
            date = DATE,
            note = "Steady day",
            updatedAtMillis = 1L,
        )

        repository.resetDate(DATE)

        assertEquals(emptyMap<Pair<String, Long>, TodayEntryEntity>(), todayEntryDao.entries)
        assertEquals(listOf(DATE), todayEntryDao.deletedDates)
        assertEquals(emptyMap<String, DailySummaryNoteEntity>(), dailySummaryNoteDao.notes)
        assertEquals(listOf(DATE), dailySummaryNoteDao.deletedDates)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    private fun todayEntry(
        id: Long = 0L,
        routineItemId: Long,
        isChecked: Boolean = false,
        completedCount: Int = 0,
        isHidden: Boolean = false,
        note: String? = null,
        updatedAtMillis: Long = 1L,
    ): TodayEntryEntity =
        TodayEntryEntity(
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

private class FakeTodayEntryDao : TodayEntryDao {
    val entries = mutableMapOf<Pair<String, Long>, TodayEntryEntity>()
    val deletedDates = mutableListOf<String>()

    override fun entriesForDate(date: String): Flow<List<TodayEntryEntity>> =
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

    override suspend fun deleteEntriesBefore(date: String) {
        entries.keys.removeAll { (entryDate, _) -> entryDate < date }
    }

    private fun updateEntry(
        date: String,
        routineItemId: Long,
        updatedAtMillis: Long,
        update: (TodayEntryEntity) -> TodayEntryEntity,
    ) {
        val key = date to routineItemId
        val existing = entries[key] ?: TodayEntryEntity(
            routineItemId = routineItemId,
            date = date,
            updatedAtMillis = updatedAtMillis,
        )
        entries[key] = update(existing).copy(updatedAtMillis = updatedAtMillis)
    }
}

private class FakeDailySummaryNoteDao : DailySummaryNoteDao {
    val notes = mutableMapOf<String, DailySummaryNoteEntity>()
    val deletedDates = mutableListOf<String>()

    override fun noteForDate(date: String): Flow<DailySummaryNoteEntity?> =
        flowOf(notes[date])

    override suspend fun upsert(note: DailySummaryNoteEntity) {
        notes[note.date] = note
    }

    override suspend fun deleteForDate(date: String) {
        notes.remove(date)
        deletedDates += date
    }
}
