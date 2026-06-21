package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineSnapshotBackfillTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val todayRepository = FakeTodayRoutineRepository()
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()

    @Test
    fun `given app starts before deadline when backfill runs then no snapshots are finalized`() = runTest {
        val now = ZonedDateTime.of(2026, 6, 1, 6, 59, 0, 0, zoneId)

        createBackfill(now).backfillMissedSnapshots(now)

        assertTrue(historyRepository.savedSnapshots.isEmpty())
        assertTrue(todayRepository.resetDates.isEmpty())
        assertTrue(weeklyRepository.resetWeeks.isEmpty())
    }

    @Test
    fun `given app starts Tuesday after deadline when backfill runs then only daily snapshot is finalized`() = runTest {
        val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, zoneId)
        seedDaily(date = "2026-06-01")

        createBackfill(now).backfillMissedSnapshots(now)

        assertEquals(
            listOf("2026-06-01" to RoutineCadence.Daily),
            historyRepository.savedSnapshots.map { snapshot -> snapshot.date to snapshot.cadence },
        )
        assertEquals(listOf("2026-06-01"), todayRepository.resetDates)
        assertTrue(weeklyRepository.resetWeeks.isEmpty())
    }

    @Test
    fun `given app starts Monday after deadline when backfill runs then daily and weekly snapshots are finalized`() = runTest {
        val now = ZonedDateTime.of(2026, 6, 1, 8, 0, 0, 0, zoneId)
        seedDaily(date = "2026-05-31")
        seedWeekly(weekStartDate = "2026-05-25")

        createBackfill(now).backfillMissedSnapshots(now)

        assertEquals(
            listOf(
                "2026-05-31" to RoutineCadence.Daily,
                "2026-05-25" to RoutineCadence.Weekly,
            ),
            historyRepository.savedSnapshots.map { snapshot -> snapshot.date to snapshot.cadence },
        )
        assertEquals(listOf("2026-05-31"), todayRepository.resetDates)
        assertEquals(listOf("2026-05-25"), weeklyRepository.resetWeeks)
    }

    private fun seedDaily(date: String) {
        todayRepository.setItems(
            date = date,
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = date,
                    isChecked = true,
                    note = null,
                ),
            ),
        )
    }

    private fun seedWeekly(weekStartDate: String) {
        weeklyRepository.setItems(
            weekStartDate = weekStartDate,
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 20L,
                    actionId = 200L,
                    title = "Review goals",
                    description = null,
                    position = 0,
                    weekStartDate = weekStartDate,
                    isChecked = true,
                    note = null,
                ),
            ),
        )
    }

    private fun createBackfill(now: ZonedDateTime): RoutineSnapshotBackfill {
        val timeProvider = FixedTimeProvider(now.toInstant(), zoneId)
        return RoutineSnapshotBackfill(
            dailySnapshotOrchestrator = DailySnapshotOrchestrator(
                finalizeTodayUseCase = FinalizeTodayUseCase(todayRepository, historyRepository),
                resetTodayUseCase = ResetTodayUseCase(todayRepository),
                timeProvider = timeProvider,
            ),
            weeklySnapshotOrchestrator = WeeklySnapshotOrchestrator(
                finalizeWeeklyUseCase = FinalizeWeeklyUseCase(weeklyRepository, historyRepository),
                resetWeeklyUseCase = ResetWeeklyUseCase(weeklyRepository),
                timeProvider = timeProvider,
            ),
            timeProvider = timeProvider,
        )
    }
}
