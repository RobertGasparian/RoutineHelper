package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshot(
    val snapshotId: Long,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineSnapshotItem>,
    val cadence: RoutineCadence,
    val summaryNote: String? = null,
)
