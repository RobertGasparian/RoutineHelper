package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklySnapshotOrchestratorTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, zoneId)
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val orchestrator = WeeklySnapshotOrchestrator(
        finalizeWeeklyUseCase = FinalizeWeeklyUseCase(weeklyRepository, historyRepository),
        resetWeeklyUseCase = ResetWeeklyUseCase(weeklyRepository),
        timeProvider = FixedTimeProvider(now.toInstant(), zoneId),
    )

    @Test
    fun `given previous week has items when orchestrated then snapshot is finalized and week is reset`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-25",
            items = listOf(weeklyItem(weekStartDate = "2026-05-25")),
        )

        orchestrator.finalizePreviousWeek(now)

        val snapshot = historyRepository.savedSnapshots.single()
        assertEquals("2026-05-25", snapshot.periodStartDate)
        assertEquals(RoutineCadence.Weekly, snapshot.cadence)
        assertEquals(now.toInstant().toEpochMilli(), snapshot.finalizedAtMillis)
        assertEquals(listOf("2026-05-25"), weeklyRepository.resetWeeks)
    }

    @Test
    fun `given previous week has no items when orchestrated then week is still reset`() = runTest {
        orchestrator.finalizePreviousWeek(now)

        assertTrue(historyRepository.savedSnapshots.isEmpty())
        assertEquals(listOf("2026-05-25"), weeklyRepository.resetWeeks)
    }

    private fun weeklyItem(weekStartDate: String): WeeklyRoutineItem =
        WeeklyRoutineItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Review goals",
            description = null,
            position = 0,
            weekStartDate = weekStartDate,
            isChecked = true,
            note = null,
        )
}
