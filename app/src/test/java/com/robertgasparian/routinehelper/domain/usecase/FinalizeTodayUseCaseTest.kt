package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizeTodayUseCaseTest {
    private val todayRepository = FakeTodayRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val useCase = FinalizeTodayUseCase(
        todayRoutineRepository = todayRepository,
        routineHistoryRepository = historyRepository,
    )

    @Test
    fun savesCurrentTodayItemsAsSnapshotAndResetsDate() = runTest {
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
        assertEquals(listOf("2026-05-29"), todayRepository.resetDates)
        assertEquals(
            SavedSnapshot(
                date = "2026-05-29",
                finalizedAtMillis = 123L,
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 100L,
                        title = "Drink water",
                        description = "Drink 3L water",
                        position = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    RoutineDaySnapshotItem(
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
    fun existingSnapshotIsNotSavedAgainButDateIsReset() = runTest {
        historyRepository.setSnapshot(
            RoutineDaySummary(
                snapshotId = 77L,
                date = "2026-05-29",
                finalizedAtMillis = 100L,
            ),
        )

        val snapshotId = useCase(
            date = "2026-05-29",
            finalizedAtMillis = 200L,
        )

        assertEquals(77L, snapshotId)
        assertEquals(listOf("2026-05-29"), todayRepository.resetDates)
        assertTrue(historyRepository.savedSnapshots.isEmpty())
    }

    @Test
    fun savesTodayItemsUnderSelectedSnapshotDate() = runTest {
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
            snapshotDate = "2026-05-27",
            finalizedAtMillis = 123L,
        )

        assertEquals("2026-05-27", historyRepository.savedSnapshots.single().date)
        assertEquals(listOf("2026-05-29"), todayRepository.resetDates)
    }
}
