package com.robertgasparian.routinehelper.domain.model

data class RoutineSnapshot(
    val snapshotId: Long,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val items: List<RoutineSnapshotItem>,
    val cadence: RoutineCadence,
    val summaryNote: String? = null,
    val rating: ReflectionRating? = null,
    val selectedTags: List<SelectedReflectionTag> = emptyList(),
    /**
     * Intentional capability seam for future snapshot reflection-editing policy.
     *
     * Keep this property even while every snapshot is editable. Future restrictions can set it
     * without changing the History reflection UI contract or exposing persistence details to
     * presentation.
     */
    val isReflectionEditable: Boolean = true,
)
