package com.robertgasparian.routinehelper.domain.model

data class ReflectionTagDefinition(
    val id: Long,
    val label: String,
    val position: Int,
    val cadence: RoutineCadence,
)
