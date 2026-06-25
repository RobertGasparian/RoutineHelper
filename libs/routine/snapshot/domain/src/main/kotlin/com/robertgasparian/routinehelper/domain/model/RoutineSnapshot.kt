package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshot(
    val snapshotId: Long,
    val date: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineSnapshotItem>,
    val cadence: RoutineCadence = RoutineCadence.Daily,
    val summaryNote: String? = null,
)
