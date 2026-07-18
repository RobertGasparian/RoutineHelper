package com.robertgasparian.routinehelper.domain.removal

data class RoutineRemovalUndoState(
    val activeSource: RoutineRemovalSource? = null,
    val pendingItemCount: Int = 0,
) {
    init {
        require(pendingItemCount >= 0) { "Pending item count cannot be negative" }
        require((activeSource == null) == (pendingItemCount == 0)) {
            "An active removal source and pending item count must be present together"
        }
    }

    fun allowsRemovalFrom(source: RoutineRemovalSource): Boolean =
        activeSource == null || activeSource == source
}
