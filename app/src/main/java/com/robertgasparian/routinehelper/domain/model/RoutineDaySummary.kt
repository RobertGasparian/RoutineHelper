package com.robertgasparian.routinehelper.domain.model

data class RoutineDaySummary(
    val snapshotId: Long,
    val date: String,
    val finalizedAtMillis: Long,
)
