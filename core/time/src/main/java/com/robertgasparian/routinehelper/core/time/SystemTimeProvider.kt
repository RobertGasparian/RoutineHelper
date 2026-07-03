package com.robertgasparian.routinehelper.core.time

import java.time.ZonedDateTime
import javax.inject.Inject

class SystemTimeProvider @Inject constructor() : TimeProvider {
    private var nowProvider: () -> ZonedDateTime = { ZonedDateTime.now() }

    internal constructor(nowProvider: () -> ZonedDateTime) : this() {
        this.nowProvider = nowProvider
    }

    override fun now(): ZonedDateTime =
        nowProvider()
}
