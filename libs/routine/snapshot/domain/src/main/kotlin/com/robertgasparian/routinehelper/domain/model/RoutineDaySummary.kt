package com.robertgasparian.routinehelper.domain.model

data class RoutineDaySummary(
    val snapshotId: Long,
    val date: String,
    val finalizedAtMillis: Long,
    val cadence: RoutineCadence = RoutineCadence.Daily,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val hasSummaryNote: Boolean = false,
)
