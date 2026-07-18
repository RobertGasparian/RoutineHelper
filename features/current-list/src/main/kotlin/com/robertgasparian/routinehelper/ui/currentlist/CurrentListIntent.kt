package com.robertgasparian.routinehelper.ui.currentlist

sealed interface CurrentListIntent {
    data object SettingsClick : CurrentListIntent

    data object ShareClick : CurrentListIntent

    data class AddItem(
        val title: String,
        val description: String?,
    ) : CurrentListIntent

    data class UpdateItem(
        val itemId: Long,
        val title: String,
        val description: String?,
    ) : CurrentListIntent

    data object AddTestItemsClick : CurrentListIntent

    data class CheckedChange(
        val itemId: Long,
        val isChecked: Boolean,
    ) : CurrentListIntent

    data class SetAllChecked(
        val isChecked: Boolean,
    ) : CurrentListIntent

    data class RemoveItem(
        val itemId: Long,
    ) : CurrentListIntent

    data class ReorderItems(
        val itemIdsInOrder: List<Long>,
    ) : CurrentListIntent

    data object ClearListConfirm : CurrentListIntent
}
