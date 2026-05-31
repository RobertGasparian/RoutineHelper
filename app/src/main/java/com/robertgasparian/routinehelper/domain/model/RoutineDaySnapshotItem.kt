package com.robertgasparian.routinehelper.domain.model

data class RoutineDaySnapshotItem(
    val actionId: Long,
    val title: String,
    val description: String?,
    val position: Int,
    val isChecked: Boolean,
    val note: String?,
    val repeatTargetCount: Int? = null,
    val completedCount: Int = 0,
)
