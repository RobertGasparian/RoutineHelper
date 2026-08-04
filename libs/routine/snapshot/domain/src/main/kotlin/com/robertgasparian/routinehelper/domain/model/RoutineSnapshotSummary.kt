package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshotSummary(
    val snapshotId: Long,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val cadence: RoutineCadence,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val hasSummaryNote: Boolean = false,
    val hasRating: Boolean = false,
    val hasSelectedTags: Boolean = false,
) {
    /** True when the snapshot contains a note, rating, or at least one selected tag. */
    val hasReflection: Boolean
        get() = hasSummaryNote || hasRating || hasSelectedTags
}
