package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinalizeWeeklyUseCaseTest {
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val useCase = FinalizeWeeklyUseCase(
        weeklyRoutineRepository = weeklyRepository,
        routineHistoryRepository = historyRepository,
    )

    @Test
    fun `given current items when finalizing week then weekly snapshot is saved`() = runTest {
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
        assertEquals(emptyList<String>(), weeklyRepository.resetWeeks)
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
    fun `given existing weekly snapshot when finalizing week then snapshot is replaced`() = runTest {
        historyRepository.setSnapshot(
            RoutineDaySummary(
                snapshotId = 77L,
                date = "2026-05-24",
                finalizedAtMillis = 100L,
                cadence = RoutineCadence.Weekly,
            ),
        )
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
                    note = "Updated weekly note.",
                ),
            ),
        )

        val snapshotId = useCase(
            weekStartDate = "2026-05-24",
            finalizedAtMillis = 200L,
        )

        assertEquals(77L, snapshotId)
        assertEquals(emptyList<String>(), weeklyRepository.resetWeeks)
        assertEquals(
            SavedSnapshot(
                date = "2026-05-24",
                finalizedAtMillis = 200L,
                cadence = RoutineCadence.Weekly,
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 100L,
                        title = "Meal prep",
                        description = null,
                        position = 0,
                        isChecked = true,
                        note = "Updated weekly note.",
                    ),
                ),
            ),
            historyRepository.savedSnapshots.single(),
        )
    }

    @Test
    fun `given selected snapshot week when finalizing week then items use selected week`() = runTest {
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
        assertEquals(emptyList<String>(), weeklyRepository.resetWeeks)
    }

    @Test
    fun `given selected snapshot week when finalizing week then current week is not reset`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-24",
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Workout",
                    description = null,
                    position = 0,
                    weekStartDate = "2026-05-24",
                    repeatTargetCount = 9,
                    completedCount = 9,
                    isChecked = true,
                    note = "Kept for debug weekly snapshot.",
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
        assertEquals(9, historyRepository.savedSnapshots.single().items.single().completedCount)
        assertEquals(
            "Kept for debug weekly snapshot.",
            historyRepository.savedSnapshots.single().items.single().note,
        )
        assertEquals(emptyList<String>(), weeklyRepository.resetWeeks)
    }

    @Test
    fun `given hidden items when finalizing week then hidden state is saved`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-24",
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Workout",
                    description = null,
                    position = 0,
                    weekStartDate = "2026-05-24",
                    isChecked = false,
                    isHidden = true,
                    note = "Travel week.",
                ),
            ),
        )

        useCase(
            weekStartDate = "2026-05-24",
            finalizedAtMillis = 123L,
        )

        assertEquals(true, historyRepository.savedSnapshots.single().items.single().isHidden)
        assertEquals("Travel week.", historyRepository.savedSnapshots.single().items.single().note)
    }

    @Test
    fun `given no current items when finalizing week then snapshot is not saved`() = runTest {
        weeklyRepository.setSummaryNote(
            weekStartDate = "2026-05-24",
            note = "Note without actions should not create a snapshot.",
        )

        val snapshotId = useCase(
            weekStartDate = "2026-05-24",
            finalizedAtMillis = 123L,
        )

        assertNull(snapshotId)
        assertEquals(emptyList<SavedSnapshot>(), historyRepository.savedSnapshots)
        assertEquals(emptyList<String>(), weeklyRepository.resetWeeks)
    }
}
