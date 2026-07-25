package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineSnapshotWithEntries
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomRoutineHistoryRepositoryTest {
    private val database = TestRoomDatabase()
    private val routineSnapshotDao = FakeRoutineSnapshotDao()
    private val repository = RoomRoutineHistoryRepository(
        database = database,
        routineSnapshotDao = routineSnapshotDao,
    )

    @Test
    fun `given stored snapshots when observing summaries then calculates completion and filters cadence`() = runTest {
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 10L,
                periodStartDate = "2026-05-29",
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
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 20L,
                periodStartDate = "2026-05-25",
                cadence = "WEEKLY",
            ),
        )

        val allSummaries = repository.snapshotSummaries().first()
        val dailySummaries = repository.snapshotSummaries(RoutineCadence.Daily).first()

        assertEquals(2, allSummaries.size)
        assertEquals(
            listOf(
                RoutineSnapshotSummary(
                    snapshotId = 10L,
                    periodStartDate = "2026-05-29",
                    finalizedAtMillis = FINALIZED_AT_MILLIS,
                    cadence = RoutineCadence.Daily,
                    completedCount = 2,
                    totalCount = 3,
                    hasSummaryNote = true,
                ),
            ),
            dailySummaries,
        )
        assertEquals(listOf("DAILY"), routineSnapshotDao.requestedCadences)
    }

    @Test
    fun `given unsupported stored cadence when observing summaries then fails explicitly`() = runTest {
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 10L,
                periodStartDate = "2026-05-29",
                cadence = "MONTHLY",
            ),
        )

        val failure = runCatching { repository.snapshotSummaries().first() }.exceptionOrNull()

        assertEquals(IllegalStateException::class.java, failure?.javaClass)
        assertEquals("Unsupported routine cadence storage value: MONTHLY", failure?.message)
    }

    @Test
    fun `given snapshot entries out of order when observing detail then maps them by position`() = runTest {
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 20L,
                periodStartDate = "2026-05-25",
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
            RoutineSnapshot(
                snapshotId = 20L,
                periodStartDate = "2026-05-25",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = RoutineCadence.Weekly,
                summaryNote = "Good week",
                items = listOf(
                    RoutineSnapshotItem(
                        actionId = 1L,
                        title = "Action 1",
                        description = "Details",
                        position = 0,
                        isChecked = true,
                        note = "Done",
                    ),
                    RoutineSnapshotItem(
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
        assertEquals(listOf(20L), routineSnapshotDao.requestedSnapshotIds)
    }

    @Test
    fun `given new snapshot when saving it then normalizes header and stores entries by position`() = runTest {
        val snapshotId = repository.saveSnapshot(
            periodStartDate = "2026-05-29",
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
            RoutineSnapshotEntity(
                id = snapshotId,
                periodStartDate = "2026-05-29",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = "DAILY",
                summaryNote = "Good day",
            ),
            routineSnapshotDao.snapshots.getValue(snapshotId),
        )
        assertEquals(listOf(1L, 2L), routineSnapshotDao.entries.getValue(snapshotId).map { it.actionId })
        assertEquals(3, routineSnapshotDao.entries.getValue(snapshotId).first().repeatTargetCountSnapshot)
        assertEquals(2, routineSnapshotDao.entries.getValue(snapshotId).first().completedCount)
        assertEquals(true, routineSnapshotDao.entries.getValue(snapshotId).first().isHidden)
        assertEquals("Almost there", routineSnapshotDao.entries.getValue(snapshotId).first().note)
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    @Test
    fun `given existing snapshot when saving same period then updates header and replaces entries`() = runTest {
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(
                id = 10L,
                periodStartDate = "2026-05-29",
                cadence = "DAILY",
                summaryNote = "Old note",
            ),
            entries = listOf(entry(snapshotId = 10L, actionId = 99L, position = 0, isChecked = true)),
        )

        val snapshotId = repository.saveSnapshot(
            periodStartDate = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS + 1,
            summaryNote = "   ",
            cadence = RoutineCadence.Daily,
            items = listOf(snapshotItem(actionId = 1L, position = 0, isChecked = false)),
        )

        assertEquals(10L, snapshotId)
        assertEquals(FINALIZED_AT_MILLIS + 1, routineSnapshotDao.snapshots.getValue(10L).finalizedAtMillis)
        assertEquals(null, routineSnapshotDao.snapshots.getValue(10L).summaryNote)
        assertEquals(listOf(10L), routineSnapshotDao.updatedSnapshotIds)
        assertEquals(listOf(10L), routineSnapshotDao.deletedEntrySnapshotIds)
        assertEquals(listOf(1L), routineSnapshotDao.entries.getValue(10L).map { it.actionId })
        assertEquals(1, database.transactionSuccesses)
    }

    @Test
    fun `given stored snapshot when updating summary note then only normalized summary changes`() = runTest {
        val originalSnapshot = snapshotEntity(
            id = 10L,
            periodStartDate = "2026-05-29",
            cadence = "DAILY",
            summaryNote = "Old note",
        )
        val originalEntries = listOf(
            entry(snapshotId = 10L, actionId = 1L, position = 0, isChecked = true),
        )
        routineSnapshotDao.storeSnapshot(
            snapshot = originalSnapshot,
            entries = originalEntries,
        )

        repository.updateSnapshotSummaryNote(
            snapshotId = 10L,
            summaryNote = "  Updated reflection  ",
        )

        assertEquals(
            originalSnapshot.copy(summaryNote = "Updated reflection"),
            routineSnapshotDao.snapshots.getValue(10L),
        )
        assertEquals(originalEntries, routineSnapshotDao.entries.getValue(10L))
        assertEquals(listOf(10L), routineSnapshotDao.updatedSummaryNoteSnapshotIds)

        repository.updateSnapshotSummaryNote(
            snapshotId = 10L,
            summaryNote = "   ",
        )

        assertEquals(null, routineSnapshotDao.snapshots.getValue(10L).summaryNote)
    }

    @Test
    fun `given stored snapshot when deleting it then removes header and entries`() = runTest {
        routineSnapshotDao.storeSnapshot(
            snapshot = snapshotEntity(id = 10L, periodStartDate = "2026-05-29", cadence = "DAILY"),
            entries = listOf(entry(snapshotId = 10L, actionId = 1L, position = 0, isChecked = true)),
        )

        repository.deleteSnapshot(snapshotId = 10L)

        assertEquals(emptyMap<Long, RoutineSnapshotEntity>(), routineSnapshotDao.snapshots)
        assertEquals(emptyMap<Long, List<RoutineSnapshotEntryEntity>>(), routineSnapshotDao.entries)
        assertEquals(listOf(10L), routineSnapshotDao.deletedSnapshotIds)
    }

    private fun snapshotEntity(
        id: Long,
        periodStartDate: String,
        cadence: String,
        summaryNote: String? = null,
    ): RoutineSnapshotEntity =
        RoutineSnapshotEntity(
            id = id,
            periodStartDate = periodStartDate,
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
    ): RoutineSnapshotEntryEntity =
        RoutineSnapshotEntryEntity(
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
    ): RoutineSnapshotItem =
        RoutineSnapshotItem(
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

private class FakeRoutineSnapshotDao : RoutineSnapshotDao {
    val snapshots = mutableMapOf<Long, RoutineSnapshotEntity>()
    val entries = mutableMapOf<Long, List<RoutineSnapshotEntryEntity>>()
    val requestedCadences = mutableListOf<String>()
    val requestedSnapshotIds = mutableListOf<Long>()
    val updatedSnapshotIds = mutableListOf<Long>()
    val updatedSummaryNoteSnapshotIds = mutableListOf<Long>()
    val deletedEntrySnapshotIds = mutableListOf<Long>()
    val deletedSnapshotIds = mutableListOf<Long>()
    private var nextSnapshotId = 1_000L

    override fun snapshotsWithEntries(): Flow<List<RoutineSnapshotWithEntries>> =
        flowOf(snapshots.values.sortedByDescending(RoutineSnapshotEntity::periodStartDate).map(::withEntries))

    override fun snapshotsWithEntries(cadence: String): Flow<List<RoutineSnapshotWithEntries>> {
        requestedCadences += cadence
        return flowOf(
            snapshots.values
                .filter { snapshot -> snapshot.cadence == cadence }
                .sortedByDescending(RoutineSnapshotEntity::periodStartDate)
                .map(::withEntries),
        )
    }

    override fun snapshot(id: Long): Flow<RoutineSnapshotWithEntries?> {
        requestedSnapshotIds += id
        return flowOf(snapshots[id]?.let(::withEntries))
    }

    override suspend fun snapshotForPeriodStartDateOnce(
        periodStartDate: String,
        cadence: String,
    ): RoutineSnapshotEntity? = findSnapshot(periodStartDate, cadence)

    override suspend fun insertSnapshot(snapshot: RoutineSnapshotEntity): Long {
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

    override suspend fun updateSummaryNote(
        snapshotId: Long,
        summaryNote: String?,
    ) {
        snapshots[snapshotId] = snapshots.getValue(snapshotId).copy(summaryNote = summaryNote)
        updatedSummaryNoteSnapshotIds += snapshotId
    }

    override suspend fun insertEntries(entries: List<RoutineSnapshotEntryEntity>) {
        entries.groupBy(RoutineSnapshotEntryEntity::snapshotId).forEach { (snapshotId, newEntries) ->
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
        snapshot: RoutineSnapshotEntity,
        entries: List<RoutineSnapshotEntryEntity> = emptyList(),
    ) {
        snapshots[snapshot.id] = snapshot
        this.entries[snapshot.id] = entries
    }

    private fun withEntries(snapshot: RoutineSnapshotEntity): RoutineSnapshotWithEntries =
        RoutineSnapshotWithEntries(
            snapshot = snapshot,
            entries = entries[snapshot.id].orEmpty(),
        )

    private fun findSnapshot(
        periodStartDate: String,
        cadence: String,
    ): RoutineSnapshotEntity? =
        snapshots.values.firstOrNull { snapshot ->
            snapshot.periodStartDate == periodStartDate && snapshot.cadence == cadence
        }
}

@Suppress("OVERRIDE_DEPRECATION")
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
