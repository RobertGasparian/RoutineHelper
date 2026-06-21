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
import org.junit.Test

class RoutineSnapshotFinalizerTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, zoneId)
    private val todayRepository = FakeTodayRoutineRepository()
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val finalizer = RoutineSnapshotFinalizer(
        finalizeTodayUseCase = FinalizeTodayUseCase(todayRepository, historyRepository),
        finalizeWeeklyUseCase = FinalizeWeeklyUseCase(weeklyRepository, historyRepository),
        resetTodayUseCase = ResetTodayUseCase(todayRepository),
        resetWeeklyUseCase = ResetWeeklyUseCase(weeklyRepository),
        timeProvider = FixedTimeProvider(now.toInstant(), zoneId),
    )

    @Test
    fun `given current time when daily snapshot is finalized then previous day is saved and reset`() = runTest {
        todayRepository.setItems(
            date = "2026-06-01",
            items = listOf(todayItem(date = "2026-06-01")),
        )

        finalizer.finalizeDaily(now)

        val snapshot = historyRepository.savedSnapshots.single()
        assertEquals("2026-06-01", snapshot.date)
        assertEquals(RoutineCadence.Daily, snapshot.cadence)
        assertEquals(now.toInstant().toEpochMilli(), snapshot.finalizedAtMillis)
        assertEquals(listOf("2026-06-01"), todayRepository.resetDates)
    }

    @Test
    fun `given current time when weekly snapshot is finalized then previous week is saved and reset`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = "2026-05-25",
            items = listOf(weeklyItem(weekStartDate = "2026-05-25")),
        )

        finalizer.finalizeWeekly(now)

        val snapshot = historyRepository.savedSnapshots.single()
        assertEquals("2026-05-25", snapshot.date)
        assertEquals(RoutineCadence.Weekly, snapshot.cadence)
        assertEquals(now.toInstant().toEpochMilli(), snapshot.finalizedAtMillis)
        assertEquals(listOf("2026-05-25"), weeklyRepository.resetWeeks)
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
