package com.robertgasparian.routinehelper.ui.tracking

data class RoutineTrackingItemUiState(
    val routineItemId: Long,
    val actionId: Long,
    val title: String,
    val description: String?,
    val repeatTargetCount: Int?,
    val completedCount: Int,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val note: String,
) {
    val isRepeatAction: Boolean = repeatTargetCount != null
}
