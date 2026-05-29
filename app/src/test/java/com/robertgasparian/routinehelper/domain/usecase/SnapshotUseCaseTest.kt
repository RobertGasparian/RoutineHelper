package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SnapshotUseCaseTest {
    private val repository = FakeRoutineHistoryRepository()

    @Test
    fun snapshotSummariesStreamsSavedSnapshots() = runTest {
        repository.saveSnapshot(
            date = "2026-05-29",
            finalizedAtMillis = 123L,
            items = emptyList(),
        )

        val summaries = SnapshotSummariesUseCase(repository)().first()

        assertEquals(1, summaries.size)
        assertEquals("2026-05-29", summaries.single().date)
        assertEquals(123L, summaries.single().finalizedAtMillis)
    }

    @Test
    fun snapshotStreamsSnapshotById() = runTest {
        val snapshotId = repository.saveSnapshot(
            date = "2026-05-29",
            finalizedAtMillis = 123L,
            items = listOf(
                RoutineDaySnapshotItem(
                    actionId = 100L,
                    title = "Drink water",
                    description = "Drink 3L water",
                    position = 0,
                    isChecked = true,
                    note = "One liter was diet soda.",
                ),
            ),
        )

        val snapshot = SnapshotUseCase(repository)(snapshotId).first()

        requireNotNull(snapshot)
        assertEquals("2026-05-29", snapshot.date)
        assertEquals("Drink water", snapshot.items.single().title)
    }
}
