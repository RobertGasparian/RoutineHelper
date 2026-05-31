package com.robertgasparian.routinehelper.work

import java.time.Duration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

internal object SnapshotWorkDates {
    val DefaultFinalizeTime: LocalTime = LocalTime.of(2, 30)
    val AppStartBackfillDeadline: LocalTime = LocalTime.of(7, 0)

    fun dailySnapshotDate(now: ZonedDateTime): LocalDate =
        now.toLocalDate().minusDays(1)

    fun previousCompletedCalendarWeekStartDate(now: ZonedDateTime): LocalDate =
        now.toLocalDate().startOfCalendarWeek().minusWeeks(1)

    fun shouldBackfillDailyOnAppStart(now: ZonedDateTime): Boolean =
        !now.toLocalTime().isBefore(AppStartBackfillDeadline)

    fun shouldBackfillWeeklyOnAppStart(now: ZonedDateTime): Boolean =
        now.dayOfWeek == DayOfWeek.MONDAY && !now.toLocalTime().isBefore(AppStartBackfillDeadline)

    fun delayUntilNextDailyFinalize(
        now: ZonedDateTime,
        finalizeTime: LocalTime = DefaultFinalizeTime,
    ): Duration {
        val targetToday = now.toLocalDate().atTime(finalizeTime).atZone(now.zone)
        val nextTarget = if (targetToday.isAfter(now)) targetToday else targetToday.plusDays(1)
        return Duration.between(now, nextTarget)
    }

    fun delayUntilNextWeeklyFinalize(
        now: ZonedDateTime,
        finalizeTime: LocalTime = DefaultFinalizeTime,
    ): Duration {
        val targetThisWeek = now.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            .atTime(finalizeTime)
            .atZone(now.zone)
        val nextTarget = if (targetThisWeek.isAfter(now)) {
            targetThisWeek
        } else {
            targetThisWeek.plusWeeks(1)
        }
        return Duration.between(now, nextTarget)
    }
}

internal fun LocalDate.startOfCalendarWeek(): LocalDate =
    with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
