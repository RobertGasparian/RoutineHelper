package com.robertgasparian.routinehelper.work

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotWorkDatesTest {
    private val zoneId = ZoneId.of("America/New_York")

    @Test
    fun dailySnapshotDateUsesPreviousDay() {
        val now = ZonedDateTime.of(2026, 5, 30, 2, 30, 0, 0, zoneId)

        assertEquals(
            LocalDate.of(2026, 5, 29),
            SnapshotWorkDates.dailySnapshotDate(now),
        )
    }

    @Test
    fun previousCompletedCalendarWeekStartDateUsesMondayOfPreviousWeek() {
        val now = ZonedDateTime.of(2026, 6, 1, 2, 30, 0, 0, zoneId)

        assertEquals(
            LocalDate.of(2026, 5, 25),
            SnapshotWorkDates.previousCompletedCalendarWeekStartDate(now),
        )
    }

    @Test
    fun dailyInitialDelayTargetsNextTwoThirty() {
        val now = ZonedDateTime.of(2026, 5, 29, 1, 30, 0, 0, zoneId)

        assertEquals(
            60L,
            SnapshotWorkDates.delayUntilNextDailyFinalize(
                now = now,
                finalizeTime = LocalTime.of(2, 30),
            ).toMinutes(),
        )
    }

    @Test
    fun weeklyInitialDelayTargetsUpcomingMondayTwoThirty() {
        val now = ZonedDateTime.of(2026, 5, 27, 2, 30, 0, 0, zoneId)

        assertEquals(
            5L,
            SnapshotWorkDates.delayUntilNextWeeklyFinalize(
                now = now,
                finalizeTime = LocalTime.of(2, 30),
            ).toDays(),
        )
    }

    @Test
    fun weeklyInitialDelayTargetsNextMondayWhenCurrentMondayCutoffPassed() {
        val now = ZonedDateTime.of(2026, 6, 1, 3, 0, 0, 0, zoneId)

        assertEquals(
            6L,
            SnapshotWorkDates.delayUntilNextWeeklyFinalize(
                now = now,
                finalizeTime = LocalTime.of(2, 30),
            ).toDays(),
        )
    }

    @Test
    fun appStartDailyBackfillStartsAtSeven() {
        assertFalse(
            SnapshotWorkDates.shouldBackfillDailyOnAppStart(
                ZonedDateTime.of(2026, 5, 30, 6, 59, 0, 0, zoneId),
            ),
        )
        assertTrue(
            SnapshotWorkDates.shouldBackfillDailyOnAppStart(
                ZonedDateTime.of(2026, 5, 30, 7, 0, 0, 0, zoneId),
            ),
        )
    }

    @Test
    fun appStartWeeklyBackfillRunsOnlyOnMondayAfterSeven() {
        assertTrue(
            SnapshotWorkDates.shouldBackfillWeeklyOnAppStart(
                ZonedDateTime.of(2026, 6, 1, 7, 0, 0, 0, zoneId),
            ),
        )
        assertFalse(
            SnapshotWorkDates.shouldBackfillWeeklyOnAppStart(
                ZonedDateTime.of(2026, 6, 2, 7, 0, 0, 0, zoneId),
            ),
        )
    }
}
