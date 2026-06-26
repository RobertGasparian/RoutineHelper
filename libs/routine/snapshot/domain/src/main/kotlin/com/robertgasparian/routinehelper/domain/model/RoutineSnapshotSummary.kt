package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshotSummary(
    val snapshotId: Long,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val cadence: RoutineCadence = RoutineCadence.Daily,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val hasSummaryNote: Boolean = false,
)
