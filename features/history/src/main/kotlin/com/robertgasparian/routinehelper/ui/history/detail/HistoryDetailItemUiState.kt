package com.robertgasparian.routinehelper.ui.history.detail

data class HistoryDetailItemUiState(
    val actionId: Long,
    val title: String,
    val description: String?,
    val repeatTargetCount: Int?,
    val completedCount: Int,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val note: String?,
) {
    val isRepeatAction: Boolean = repeatTargetCount != null

    val isComplete: Boolean = !isHidden && if (isRepeatAction) {
        completedCount >= repeatTargetCount.orZero()
    } else {
        isChecked
    }
}

private fun Int?.orZero(): Int = this ?: 0
