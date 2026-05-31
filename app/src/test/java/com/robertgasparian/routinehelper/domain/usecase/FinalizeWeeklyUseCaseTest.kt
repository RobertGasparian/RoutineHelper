package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizeWeeklyUseCaseTest {
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val useCase = FinalizeWeeklyUseCase(
        weeklyRoutineRepository = weeklyRepository,
        routineHistoryRepository = historyRepository,
    )

    @Test
    fun savesCurrentWeeklyItemsAsWeeklySnapshotAndResetsWeek() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-24",
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Meal prep",
                    description = "Cook lunches",
                    position = 0,
                    weekStartDate = "2026-05-24",
                    isChecked = true,
                    note = "Prepared four portions.",
                ),
                WeeklyRoutineItem(
                    routineItemId = 11L,
                    actionId = 101L,
                    title = "Budget review",
                    description = null,
                    position = 1,
                    weekStartDate = "2026-05-24",
                    isChecked = false,
                    note = null,
                ),
            ),
        )
        weeklyRepository.setSummaryNote(
            weekStartDate = "2026-05-24",
            note = "Solid weekly reset.",
        )

        val snapshotId = useCase(
            weekStartDate = "2026-05-24",
            finalizedAtMillis = 123L,
        )

        assertEquals(1L, snapshotId)
        assertEquals(listOf("2026-05-24"), weeklyRepository.resetWeeks)
        assertEquals(
            SavedSnapshot(
                date = "2026-05-24",
                finalizedAtMillis = 123L,
                summaryNote = "Solid weekly reset.",
                cadence = RoutineCadence.Weekly,
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 100L,
                        title = "Meal prep",
                        description = "Cook lunches",
                        position = 0,
                        isChecked = true,
                        note = "Prepared four portions.",
                    ),
                    RoutineDaySnapshotItem(
                        actionId = 101L,
                        title = "Budget review",
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
    fun existingWeeklySnapshotIsNotSavedAgainButWeekIsReset() = runTest {
        historyRepository.setSnapshot(
            RoutineDaySummary(
                snapshotId = 77L,
                date = "2026-05-24",
                finalizedAtMillis = 100L,
                cadence = RoutineCadence.Weekly,
            ),
        )

        val snapshotId = useCase(
            weekStartDate = "2026-05-24",
            finalizedAtMillis = 200L,
        )

        assertEquals(77L, snapshotId)
        assertEquals(listOf("2026-05-24"), weeklyRepository.resetWeeks)
        assertTrue(historyRepository.savedSnapshots.isEmpty())
    }

    @Test
    fun savesWeeklyItemsUnderSelectedSnapshotWeek() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-24",
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Meal prep",
                    description = null,
                    position = 0,
                    weekStartDate = "2026-05-24",
                    isChecked = true,
                    note = null,
                ),
            ),
        )

        useCase(
            weekStartDate = "2026-05-24",
            snapshotWeekStartDate = "2026-05-17",
            finalizedAtMillis = 123L,
        )

        assertEquals("2026-05-17", historyRepository.savedSnapshots.single().date)
        assertEquals(RoutineCadence.Weekly, historyRepository.savedSnapshots.single().cadence)
        assertEquals(listOf("2026-05-24"), weeklyRepository.resetWeeks)
    }
}
