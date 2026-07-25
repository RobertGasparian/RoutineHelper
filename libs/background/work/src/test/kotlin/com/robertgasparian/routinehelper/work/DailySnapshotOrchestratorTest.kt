package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import java.time.ZonedDateTime
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailySnapshotOrchestratorTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, zoneId)
    private val todayRepository = FakeTodayRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val orchestrator = DailySnapshotOrchestrator(
        finalizeTodayUseCase = FinalizeTodayUseCase(todayRepository, historyRepository),
        resetTodayUseCase = ResetTodayUseCase(todayRepository),
        snapshotSummariesUseCase = SnapshotSummariesUseCase(historyRepository),
        timeProvider = FixedTimeProvider(now.toInstant(), zoneId),
    )

    @Test
    fun `given previous day has items when orchestrated then snapshot is finalized and day is reset`() = runTest {
        todayRepository.setItems(
            date = "2026-06-01",
            items = listOf(todayItem(date = "2026-06-01")),
        )

        orchestrator.finalizePreviousDay(now)

        val snapshot = historyRepository.savedSnapshots.single()
        assertEquals("2026-06-01", snapshot.periodStartDate)
        assertEquals(RoutineCadence.Daily, snapshot.cadence)
        assertEquals(now.toInstant().toEpochMilli(), snapshot.finalizedAtMillis)
        assertEquals(listOf("2026-06-01"), todayRepository.resetDates)
    }

    @Test
    fun `given previous day has no items when orchestrated then day is still reset`() = runTest {
        orchestrator.finalizePreviousDay(now)

        assertTrue(historyRepository.savedSnapshots.isEmpty())
        assertEquals(listOf("2026-06-01"), todayRepository.resetDates)
    }

    @Test
    fun `given previous day snapshot already exists when orchestrated then snapshot is preserved and day is reset`() =
        runTest {
            historyRepository.setSnapshot(
                RoutineSnapshot(
                    snapshotId = 42L,
                    periodStartDate = "2026-06-01",
                    finalizedAtMillis = 100L,
                    cadence = RoutineCadence.Daily,
                    items = emptyList(),
                    summaryNote = "Already finalized",
                ),
            )
            todayRepository.setItems(
                date = "2026-06-01",
                items = listOf(todayItem(date = "2026-06-01")),
            )

            orchestrator.finalizePreviousDay(now)

            assertTrue(historyRepository.savedSnapshots.isEmpty())
            assertEquals(listOf("2026-06-01"), todayRepository.resetDates)
        }

    private fun todayItem(date: String): TodayRoutineItem =
        TodayRoutineItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            description = null,
            position = 0,
            date = date,
            isChecked = true,
            note = null,
        )
}
