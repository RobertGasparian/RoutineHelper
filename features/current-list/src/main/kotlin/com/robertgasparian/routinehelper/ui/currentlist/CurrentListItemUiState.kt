package com.robertgasparian.routinehelper.ui.currentlist

data class CurrentListItemUiState(
    val id: Long,
    val title: String,
    val description: String?,
    val isChecked: Boolean,
)
