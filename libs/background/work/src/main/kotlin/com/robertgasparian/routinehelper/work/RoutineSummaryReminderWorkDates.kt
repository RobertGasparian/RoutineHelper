package com.robertgasparian.routinehelper.work

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

internal object RoutineSummaryReminderWorkDates {
    val DefaultReminderTime: LocalTime = LocalTime.of(10, 0)

    fun delayUntilNextDailyReminder(
        now: ZonedDateTime,
        reminderTime: LocalTime = DefaultReminderTime,
    ): Duration {
        val targetToday = now.toLocalDate().atTime(reminderTime).atZone(now.zone)
        val nextTarget = if (targetToday.isAfter(now)) targetToday else targetToday.plusDays(1)
        return Duration.between(now, nextTarget)
    }

    fun delayUntilNextWeeklyReminder(
        now: ZonedDateTime,
        reminderTime: LocalTime = DefaultReminderTime,
    ): Duration {
        val targetThisWeek = now.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            .atTime(reminderTime)
            .atZone(now.zone)
        val nextTarget = if (targetThisWeek.isAfter(now)) {
            targetThisWeek
        } else {
            targetThisWeek.plusWeeks(1)
        }
        return Duration.between(now, nextTarget)
    }
}
