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
    fun `given configured clock when reading now then returns configured zoned time`() {
        assertEquals(
            instant.atZone(zoneId),
            timeProvider.now(),
        )
    }

    @Test
    fun `given configured clock zone when reading current date then returns date in that zone`() {
        assertEquals(
            "2026-05-29",
            timeProvider.currentDate().toString(),
        )
    }

    @Test
    fun `given configured clock when reading current time millis then returns configured epoch millis`() {
        assertEquals(
            instant.toEpochMilli(),
            timeProvider.currentTimeMillis(),
        )
    }
}
