package com.robertgasparian.routinehelper.domain.time

import com.robertgasparian.routinehelper.core.time.startOfCalendarWeek
import java.time.LocalDate
import java.time.ZonedDateTime

object SnapshotDates {
    fun dailySnapshotDate(now: ZonedDateTime): LocalDate =
        now.toLocalDate().minusDays(1)

    fun previousCompletedCalendarWeekStartDate(now: ZonedDateTime): LocalDate =
        now.toLocalDate().startOfCalendarWeek().minusWeeks(1)
}
