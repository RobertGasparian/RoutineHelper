package com.robertgasparian.routinehelper.domain.model

data class RoutineDaySnapshot(
    val snapshotId: Long,
    val date: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineDaySnapshotItem>,
)
