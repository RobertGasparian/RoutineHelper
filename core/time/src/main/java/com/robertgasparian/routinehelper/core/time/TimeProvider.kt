package com.robertgasparian.routinehelper.core.time

import java.time.LocalDate
import java.time.ZonedDateTime

interface TimeProvider {
    fun now(): ZonedDateTime

    fun currentDate(): LocalDate =
        now().toLocalDate()

    fun currentTimeMillis(): Long =
        now().toInstant().toEpochMilli()
}
