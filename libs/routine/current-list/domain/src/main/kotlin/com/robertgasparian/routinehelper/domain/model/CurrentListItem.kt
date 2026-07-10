package com.robertgasparian.routinehelper.domain.model

data class CurrentListItem(
    val id: Long,
    val title: String,
    val description: String?,
    val position: Int,
    val isChecked: Boolean,
)
