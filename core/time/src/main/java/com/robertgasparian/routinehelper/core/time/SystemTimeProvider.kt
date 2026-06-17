package com.robertgasparian.routinehelper.core.time

import java.time.ZonedDateTime

class SystemTimeProvider(
    private val nowProvider: () -> ZonedDateTime = { ZonedDateTime.now() },
) : TimeProvider {
    override fun now(): ZonedDateTime =
        nowProvider()
}
