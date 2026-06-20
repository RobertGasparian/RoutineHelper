package com.robertgasparian.routinehelper.ui.actioneditor

data class ActionEditorUiState(
    val title: String = "",
    val description: String = "",
    val isRepeatEnabled: Boolean = false,
    val repeatTargetCount: Int = 2,
    val isEditing: Boolean = false,
) {
    val canSave: Boolean = title.isNotBlank()
    val savedRepeatTargetCount: Int? = repeatTargetCount.takeIf { isRepeatEnabled && it > 1 }

    companion object {
        fun preview(): ActionEditorUiState =
            ActionEditorUiState(
                title = "Drink water",
                description = "Drink 3L water",
                isRepeatEnabled = true,
                repeatTargetCount = 3,
                isEditing = true,
            )

        fun previewEmpty(): ActionEditorUiState =
            ActionEditorUiState()
    }
}
