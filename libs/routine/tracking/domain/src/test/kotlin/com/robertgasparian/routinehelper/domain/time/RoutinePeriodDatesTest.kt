package com.robertgasparian.routinehelper.domain.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutinePeriodDatesTest {
    @Test
    fun `given midweek date when finding calendar week start then previous Monday is returned`() {
        assertEquals(
            LocalDate.of(2026, 5, 25),
            LocalDate.of(2026, 5, 29).startOfCalendarWeek(),
        )
    }
}
