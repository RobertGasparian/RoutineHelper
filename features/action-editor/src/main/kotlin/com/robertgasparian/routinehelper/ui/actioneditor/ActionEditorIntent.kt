package com.robertgasparian.routinehelper.ui.actioneditor

sealed interface ActionEditorIntent {
    data object BackClick : ActionEditorIntent

    data class TitleChange(
        val title: String,
    ) : ActionEditorIntent

    data class DescriptionChange(
        val description: String,
    ) : ActionEditorIntent

    data class RepeatEnabledChange(
        val enabled: Boolean,
    ) : ActionEditorIntent

    data class RepeatTargetCountChange(
        val targetCount: Int,
    ) : ActionEditorIntent

    data object SaveClick : ActionEditorIntent

    data object DeleteClick : ActionEditorIntent
}
