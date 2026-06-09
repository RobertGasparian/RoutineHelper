package com.robertgasparian.routinehelper.domain.model

data class RoutineDaySnapshotItem(
    val actionId: Long,
    val title: String,
    val description: String?,
    val position: Int,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val note: String?,
    val repeatTargetCount: Int? = null,
    val completedCount: Int = 0,
)
