package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshot(
    val snapshotId: Long,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineSnapshotItem>,
    val cadence: RoutineCadence,
    val summaryNote: String? = null,
    /**
     * Intentional capability seam for future snapshot-editing policy.
     *
     * Keep this property even while every snapshot is editable. Future restrictions can set it
     * without changing the History UI contract or exposing persistence details to presentation.
     */
    val isSummaryNoteEditable: Boolean = true,
)
