package com.robertgasparian.routinehelper.core.time

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemTimeProviderTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val instant = Instant.parse("2026-05-29T14:30:00Z")
    private val timeProvider = SystemTimeProvider(
        nowProvider = { instant.atZone(zoneId) },
    )

    @Test
    fun nowUsesConfiguredClock() {
        assertEquals(
            instant.atZone(zoneId),
            timeProvider.now(),
        )
    }

    @Test
    fun currentDateUsesConfiguredClockZone() {
        assertEquals(
            "2026-05-29",
            timeProvider.currentDate().toString(),
        )
    }

    @Test
    fun currentTimeMillisUsesConfiguredClock() {
        assertEquals(
            instant.toEpochMilli(),
            timeProvider.currentTimeMillis(),
        )
    }
}
