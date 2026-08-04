package com.robertgasparian.routinehelper.domain.model

/**
 * A tag selected for one reflection.
 *
 * [templateTagId] identifies a live cadence-template tag for current Daily/Weekly reflections.
 * Snapshot repositories deliberately persist only [label] and [position], so History tags remain
 * entry-local and never retain a data relationship with the cadence template.
 */
data class SelectedReflectionTag(
    val label: String,
    val position: Int,
    val templateTagId: Long? = null,
)
