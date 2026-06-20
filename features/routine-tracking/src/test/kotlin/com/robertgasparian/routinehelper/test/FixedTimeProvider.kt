package com.robertgasparian.routinehelper.test

import com.robertgasparian.routinehelper.core.time.TimeProvider
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class FixedTimeProvider(
    private val instant: Instant = Instant.parse("2026-05-29T14:30:00Z"),
    private val zoneId: ZoneId = ZoneId.of("America/New_York"),
) : TimeProvider {
    override fun now(): ZonedDateTime =
        instant.atZone(zoneId)
}
