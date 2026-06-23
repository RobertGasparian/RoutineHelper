package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.DailySnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.DailySnapshotWithEntries
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomRoutineHistoryRepositoryTest {
    private val database = TestRoomDatabase()
    private val dailySnapshotDao = FakeDailySnapshotDao()
    private val repository = RoomRoutineHistoryRepository(
        database = database,
        dailySnapshotDao = dailySnapshotDao,
    )

    @Test
    fun `given stored snapshots when observing summaries then calculates completion and filters cadence`() = runTest {
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 10L,
                date = "2026-05-29",
                cadence = "DAILY",
                summaryNote = "Good day",
            ),
            entries = listOf(
                entry(snapshotId = 10L, actionId = 1L, position = 0, isChecked = true),
                entry(
                    snapshotId = 10L,
                    actionId = 2L,
                    position = 1,
                    isChecked = false,
                    repeatTargetCount = 3,
                    completedCount = 3,
                ),
                entry(snapshotId = 10L, actionId = 3L, position = 2, isChecked = false),
                entry(
                    snapshotId = 10L,
                    actionId = 4L,
                    position = 3,
                    isChecked = true,
                    isHidden = true,
                ),
            ),
        )
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 20L,
                date = "2026-05-25",
                cadence = "WEEKLY",
            ),
        )

        val allSummaries = repository.snapshotSummaries().first()
        val dailySummaries = repository.snapshotSummaries(RoutineCadence.Daily).first()

        assertEquals(2, allSummaries.size)
        assertEquals(
            listOf(
                RoutineDaySummary(
                    snapshotId = 10L,
                    date = "2026-05-29",
                    finalizedAtMillis = FINALIZED_AT_MILLIS,
                    cadence = RoutineCadence.Daily,
                    completedCount = 2,
                    totalCount = 3,
                    hasSummaryNote = true,
                ),
            ),
            dailySummaries,
        )
        assertEquals(listOf("DAILY"), dailySnapshotDao.requestedCadences)
    }

    @Test
    fun `given snapshot entries out of order when observing detail then maps them by position`() = runTest {
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 20L,
                date = "2026-05-25",
                cadence = "WEEKLY",
                summaryNote = "Good week",
            ),
            entries = listOf(
                entry(snapshotId = 20L, actionId = 2L, position = 1, isChecked = false),
                entry(
                    snapshotId = 20L,
                    actionId = 1L,
                    position = 0,
                    isChecked = true,
                    description = "Details",
                    note = "Done",
                ),
            ),
        )

        val snapshot = repository.snapshot(snapshotId = 20L).first()

        assertEquals(
            RoutineDaySnapshot(
                snapshotId = 20L,
                date = "2026-05-25",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = RoutineCadence.Weekly,
                summaryNote = "Good week",
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 1L,
                        title = "Action 1",
                        description = "Details",
                        position = 0,
                        isChecked = true,
                        note = "Done",
                    ),
                    RoutineDaySnapshotItem(
                        actionId = 2L,
                        title = "Action 2",
                        description = null,
                        position = 1,
                        isChecked = false,
                        note = null,
                    ),
                ),
            ),
            snapshot,
        )
        assertEquals(listOf(20L), dailySnapshotDao.requestedSnapshotIds)
    }

    @Test
    fun `given snapshot header when finding by date then maps cadence and summary-note presence`() = runTest {
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 20L,
                date = "2026-05-25",
                cadence = "WEEKLY",
                summaryNote = "   ",
            ),
        )

        val summary = repository.snapshotForDate(
            date = "2026-05-25",
            cadence = RoutineCadence.Weekly,
        ).first()

        assertEquals(
            RoutineDaySummary(
                snapshotId = 20L,
                date = "2026-05-25",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = RoutineCadence.Weekly,
                hasSummaryNote = false,
            ),
            summary,
        )
        assertEquals(listOf("2026-05-25" to "WEEKLY"), dailySnapshotDao.requestedDates)
    }

    @Test
    fun `given new snapshot when saving it then normalizes header and stores entries by position`() = runTest {
        val snapshotId = repository.saveSnapshot(
            date = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            summaryNote = "  Good day  ",
            cadence = RoutineCadence.Daily,
            items = listOf(
                snapshotItem(actionId = 2L, position = 1, isChecked = false),
                snapshotItem(
                    actionId = 1L,
                    position = 0,
                    isChecked = true,
                    repeatTargetCount = 3,
                    completedCount = 2,
                    isHidden = true,
                    note = "Almost there",
                ),
            ),
        )

        assertEquals(
            DailySnapshotEntity(
                id = snapshotId,
                date = "2026-05-29",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = "DAILY",
                summaryNote = "Good day",
            ),
            dailySnapshotDao.snapshots.getValue(snapshotId),
        )
        assertEquals(listOf(1L, 2L), dailySnapshotDao.entries.getValue(snapshotId).map { it.actionId })
        assertEquals(3, dailySnapshotDao.entries.getValue(snapshotId).first().repeatTargetCountSnapshot)
        assertEquals(2, dailySnapshotDao.entries.getValue(snapshotId).first().completedCount)
        assertEquals(true, dailySnapshotDao.entries.getValue(snapshotId).first().isHidden)
        assertEquals("Almost there", dailySnapshotDao.entries.getValue(snapshotId).first().note)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    @Test
    fun `given existing snapshot when saving same period then updates header and replaces entries`() = runTest {
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 10L,
                date = "2026-05-29",
                cadence = "DAILY",
                summaryNote = "Old note",
            ),
            entries = listOf(entry(snapshotId = 10L, actionId = 99L, position = 0, isChecked = true)),
        )

        val snapshotId = repository.saveSnapshot(
            date = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS + 1,
            summaryNote = "   ",
            cadence = RoutineCadence.Daily,
            items = listOf(snapshotItem(actionId = 1L, position = 0, isChecked = false)),
        )

        assertEquals(10L, snapshotId)
        assertEquals(FINALIZED_AT_MILLIS + 1, dailySnapshotDao.snapshots.getValue(10L).finalizedAtMillis)
        assertEquals(null, dailySnapshotDao.snapshots.getValue(10L).summaryNote)
        assertEquals(listOf(10L), dailySnapshotDao.updatedSnapshotIds)
        assertEquals(listOf(10L), dailySnapshotDao.deletedEntrySnapshotIds)
        assertEquals(listOf(1L), dailySnapshotDao.entries.getValue(10L).map { it.actionId })
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `given stored snapshot when deleting it then removes header and entries`() = runTest {
        dailySnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(id = 10L, date = "2026-05-29", cadence = "DAILY"),
            entries = listOf(entry(snapshotId = 10L, actionId = 1L, position = 0, isChecked = true)),
        )

        repository.deleteSnapshot(snapshotId = 10L)

        assertEquals(emptyMap<Long, DailySnapshotEntity>(), dailySnapshotDao.snapshots)
        assertEquals(emptyMap<Long, List<DailySnapshotEntryEntity>>(), dailySnapshotDao.entries)
        assertEquals(listOf(10L), dailySnapshotDao.deletedSnapshotIds)
    }

    private fun snapshotEntity(
        id: Long,
        date: String,
        cadence: String,
        summaryNote: String? = null,
    ): DailySnapshotEntity =
        DailySnapshotEntity(
            id = id,
            date = date,
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = cadence,
            summaryNote = summaryNote,
        )

    private fun entry(
        snapshotId: Long,
        actionId: Long,
        position: Int,
        isChecked: Boolean,
        description: String? = null,
        isHidden: Boolean = false,
        repeatTargetCount: Int? = null,
        completedCount: Int = 0,
        note: String? = null,
    ): DailySnapshotEntryEntity =
        DailySnapshotEntryEntity(
            snapshotId = snapshotId,
            actionId = actionId,
            titleSnapshot = "Action $actionId",
            descriptionSnapshot = description,
            positionSnapshot = position,
            isChecked = isChecked,
            isHidden = isHidden,
            repeatTargetCountSnapshot = repeatTargetCount,
            completedCount = completedCount,
            note = note,
        )

    private fun snapshotItem(
        actionId: Long,
        position: Int,
        isChecked: Boolean,
        repeatTargetCount: Int? = null,
        completedCount: Int = 0,
        isHidden: Boolean = false,
        note: String? = null,
    ): RoutineDaySnapshotItem =
        RoutineDaySnapshotItem(
            actionId = actionId,
            title = "Action $actionId",
            description = null,
            position = position,
            isChecked = isChecked,
            isHidden = isHidden,
            repeatTargetCount = repeatTargetCount,
            completedCount = completedCount,
            note = note,
        )

    private companion object {
        const val FINALIZED_AT_MILLIS = 1_748_531_800_000L
    }
}

private class FakeDailySnapshotDao : DailySnapshotDao {
    val snapshots = mutableMapOf<Long, DailySnapshotEntity>()
    val entries = mutableMapOf<Long, List<DailySnapshotEntryEntity>>()
    val requestedCadences = mutableListOf<String>()
    val requestedDates = mutableListOf<Pair<String, String>>()
    val requestedSnapshotIds = mutableListOf<Long>()
    val updatedSnapshotIds = mutableListOf<Long>()
    val deletedEntrySnapshotIds = mutableListOf<Long>()
    val deletedSnapshotIds = mutableListOf<Long>()
    private var nextSnapshotId = 1_000L

    override fun snapshots(): Flow<List<DailySnapshotEntity>> =
        flowOf(snapshots.values.sortedByDescending(DailySnapshotEntity::date))

    override fun snapshots(cadence: String): Flow<List<DailySnapshotEntity>> =
        flowOf(snapshots.values.filter { it.cadence == cadence }.sortedByDescending(DailySnapshotEntity::date))

    override fun snapshotsWithEntries(): Flow<List<DailySnapshotWithEntries>> =
        flowOf(snapshots.values.sortedByDescending(DailySnapshotEntity::date).map(::withEntries))

    override fun snapshotsWithEntries(cadence: String): Flow<List<DailySnapshotWithEntries>> {
        requestedCadences += cadence
        return flowOf(
            snapshots.values
                .filter { snapshot -> snapshot.cadence == cadence }
                .sortedByDescending(DailySnapshotEntity::date)
                .map(::withEntries),
        )
    }

    override fun snapshot(id: Long): Flow<DailySnapshotWithEntries?> {
        requestedSnapshotIds += id
        return flowOf(snapshots[id]?.let(::withEntries))
    }

    override fun snapshotForDate(
        date: String,
        cadence: String,
    ): Flow<DailySnapshotEntity?> {
        requestedDates += date to cadence
        return flowOf(findSnapshot(date, cadence))
    }

    override suspend fun snapshotForDateOnce(
        date: String,
        cadence: String,
    ): DailySnapshotEntity? = findSnapshot(date, cadence)

    override suspend fun insertSnapshot(snapshot: DailySnapshotEntity): Long {
        val id = snapshot.id.takeIf { it != 0L } ?: nextSnapshotId++
        snapshots[id] = snapshot.copy(id = id)
        return id
    }

    override suspend fun updateSnapshot(
        id: Long,
        finalizedAtMillis: Long,
        summaryNote: String?,
    ) {
        snapshots[id] = snapshots.getValue(id).copy(
            finalizedAtMillis = finalizedAtMillis,
            summaryNote = summaryNote,
        )
        updatedSnapshotIds += id
    }

    override suspend fun insertEntries(entries: List<DailySnapshotEntryEntity>) {
        entries.groupBy(DailySnapshotEntryEntity::snapshotId).forEach { (snapshotId, newEntries) ->
            this.entries[snapshotId] = this.entries[snapshotId].orEmpty() + newEntries
        }
    }

    override suspend fun deleteEntries(snapshotId: Long) {
        entries.remove(snapshotId)
        deletedEntrySnapshotIds += snapshotId
    }

    override suspend fun deleteSnapshot(id: Long) {
        snapshots.remove(id)
        entries.remove(id)
        deletedSnapshotIds += id
    }

    fun storeSnapshot(
        snapshot: DailySnapshotEntity,
        entries: List<DailySnapshotEntryEntity> = emptyList(),
    ) {
        snapshots[snapshot.id] = snapshot
        this.entries[snapshot.id] = entries
    }

    private fun withEntries(snapshot: DailySnapshotEntity): DailySnapshotWithEntries =
        DailySnapshotWithEntries(
            snapshot = snapshot,
            entries = entries[snapshot.id].orEmpty(),
        )

    private fun findSnapshot(
        date: String,
        cadence: String,
    ): DailySnapshotEntity? =
        snapshots.values.firstOrNull { snapshot ->
            snapshot.date == date && snapshot.cadence == cadence
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
