package com.robertgasparian.routinehelper.domain.model

data class RoutineReflection(
    val summaryNote: String? = null,
    val rating: ReflectionRating? = null,
) {
    val isEmpty: Boolean
        get() = summaryNote.isNullOrBlank() && rating == null
}
