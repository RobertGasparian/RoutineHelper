package com.robertgasparian.routinehelper.ui.actioneditor

data class ActionEditorUiState(
    val title: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
) {
    val canSave: Boolean = title.isNotBlank()

    companion object {
        fun preview(): ActionEditorUiState =
            ActionEditorUiState(
                title = "Drink water",
                description = "Drink 3L water",
                isEditing = true,
            )

        fun previewEmpty(): ActionEditorUiState =
            ActionEditorUiState()
    }
}
