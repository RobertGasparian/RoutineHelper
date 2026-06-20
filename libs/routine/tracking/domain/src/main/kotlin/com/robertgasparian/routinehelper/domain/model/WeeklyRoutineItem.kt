package com.robertgasparian.routinehelper.domain.model

data class WeeklyRoutineItem(
    val routineItemId: Long,
    val actionId: Long,
    val title: String,
    val description: String?,
    val position: Int,
    val weekStartDate: String,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val note: String?,
    val repeatTargetCount: Int? = null,
    val completedCount: Int = 0,
)
