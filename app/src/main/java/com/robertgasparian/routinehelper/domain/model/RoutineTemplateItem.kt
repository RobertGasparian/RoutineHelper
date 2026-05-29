package com.robertgasparian.routinehelper.domain.model

data class RoutineTemplateItem(
    val routineItemId: Long,
    val actionId: Long,
    val title: String,
    val description: String?,
    val position: Int,
)
