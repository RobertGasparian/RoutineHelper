package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinalizeTodayUseCaseTest {
    private val todayRepository = FakeTodayRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val useCase = FinalizeTodayUseCase(
        todayRoutineRepository = todayRepository,
        routineHistoryRepository = historyRepository,
    )

    @Test
    fun `given current items when finalizing today then snapshot is saved`() = runTest {
        todayRepository.setItems(
            date = "2026-05-29",
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = "Drink 3L water",
                    position = 0,
                    date = "2026-05-29",
                    isChecked = true,
                    note = "One liter was diet soda.",
                ),
                TodayRoutineItem(
                    routineItemId = 11L,
                    actionId = 101L,
                    title = "Stretch",
                    description = null,
                    position = 1,
                    date = "2026-05-29",
                    isChecked = false,
                    note = null,
                ),
            ),
        )
        todayRepository.setSummaryNote(
            date = "2026-05-29",
            note = "Low-energy day, but I kept the basics moving.",
        )

        val snapshotId = useCase(
            date = "2026-05-29",
            finalizedAtMillis = 123L,
        )

        assertEquals(1L, snapshotId)
        assertEquals(emptyList<String>(), todayRepository.resetDates)
        assertEquals(
            SavedSnapshot(
                periodStartDate = "2026-05-29",
                finalizedAtMillis = 123L,
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineSnapshotItem(
                        actionId = 100L,
                        title = "Drink water",
                        description = "Drink 3L water",
                        position = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    RoutineSnapshotItem(
                        actionId = 101L,
                        title = "Stretch",
                        description = null,
                        position = 1,
                        isChecked = false,
                        note = null,
                    ),
                ),
            ),
            historyRepository.savedSnapshots.single(),
        )
    }

    @Test
    fun `given existing daily snapshot when finalizing today then snapshot is replaced`() = runTest {
        historyRepository.setSnapshot(
            RoutineSnapshotSummary(
                snapshotId = 77L,
                periodStartDate = "2026-05-29",
                finalizedAtMillis = 100L,
            ),
        )
        todayRepository.setItems(
            date = "2026-05-29",
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = "2026-05-29",
                    isChecked = true,
                    note = "Updated note.",
                ),
            ),
        )

        val snapshotId = useCase(
            date = "2026-05-29",
            finalizedAtMillis = 200L,
        )

        assertEquals(77L, snapshotId)
        assertEquals(emptyList<String>(), todayRepository.resetDates)
        assertEquals(
            SavedSnapshot(
                periodStartDate = "2026-05-29",
                finalizedAtMillis = 200L,
                items = listOf(
                    RoutineSnapshotItem(
                        actionId = 100L,
                        title = "Drink water",
                        description = null,
                        position = 0,
                        isChecked = true,
                        note = "Updated note.",
                    ),
                ),
            ),
            historyRepository.savedSnapshots.single(),
        )
    }

    @Test
    fun `given selected snapshot date when finalizing today then items use selected date`() = runTest {
        todayRepository.setItems(
            date = "2026-05-29",
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = "2026-05-29",
                    isChecked = true,
                    note = null,
                ),
            ),
        )

        useCase(
            date = "2026-05-29",
            snapshotPeriodStartDate = "2026-05-27",
            finalizedAtMillis = 123L,
        )

        assertEquals("2026-05-27", historyRepository.savedSnapshots.single().periodStartDate)
        assertEquals(emptyList<String>(), todayRepository.resetDates)
    }

    @Test
    fun `given selected snapshot date when finalizing today then current date is not reset`() = runTest {
        todayRepository.setItems(
            date = "2026-05-29",
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = "2026-05-29",
                    isChecked = true,
                    note = "Kept for debug snapshot.",
                ),
            ),
        )

        useCase(
            date = "2026-05-29",
            snapshotPeriodStartDate = "2026-05-28",
            finalizedAtMillis = 123L,
        )

        assertEquals("2026-05-28", historyRepository.savedSnapshots.single().periodStartDate)
        assertEquals(
            "Kept for debug snapshot.",
            historyRepository.savedSnapshots.single().items.single().note,
        )
        assertEquals(emptyList<String>(), todayRepository.resetDates)
    }

    @Test
    fun `given hidden items when finalizing today then hidden state is saved`() = runTest {
        todayRepository.setItems(
            date = "2026-05-29",
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Run",
                    description = null,
                    position = 0,
                    date = "2026-05-29",
                    isChecked = false,
                    isHidden = true,
                    note = "Rest day.",
                ),
            ),
        )

        useCase(
            date = "2026-05-29",
            finalizedAtMillis = 123L,
        )

        assertEquals(true, historyRepository.savedSnapshots.single().items.single().isHidden)
        assertEquals("Rest day.", historyRepository.savedSnapshots.single().items.single().note)
    }

    @Test
    fun `given no current items when finalizing today then snapshot is not saved`() = runTest {
        todayRepository.setSummaryNote(
            date = "2026-05-29",
            note = "Note without actions should not create a snapshot.",
        )

        val snapshotId = useCase(
            date = "2026-05-29",
            finalizedAtMillis = 123L,
        )

        assertNull(snapshotId)
        assertEquals(emptyList<SavedSnapshot>(), historyRepository.savedSnapshots)
        assertEquals(emptyList<String>(), todayRepository.resetDates)
    }
}
