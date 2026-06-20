package com.robertgasparian.routinehelper.domain.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SnapshotDatesTest {
    private val zoneId = ZoneId.of("America/New_York")

    @Test
    fun `given current date when finding daily snapshot date then previous day is returned`() {
        val now = ZonedDateTime.of(2026, 5, 30, 2, 30, 0, 0, zoneId)

        assertEquals(
            LocalDate.of(2026, 5, 29),
            SnapshotDates.dailySnapshotDate(now),
        )
    }

    @Test
    fun `given current week when finding completed snapshot week then previous Monday is returned`() {
        val now = ZonedDateTime.of(2026, 6, 1, 2, 30, 0, 0, zoneId)

        assertEquals(
            LocalDate.of(2026, 5, 25),
            SnapshotDates.previousCompletedCalendarWeekStartDate(now),
        )
    }
}
