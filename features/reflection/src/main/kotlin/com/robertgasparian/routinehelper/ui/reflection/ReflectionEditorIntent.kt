package com.robertgasparian.routinehelper.ui.reflection

sealed interface ReflectionEditorIntent {
    data class DraftChange(
        val text: String,
        val selectionStart: Int,
        val selectionEnd: Int,
    ) : ReflectionEditorIntent

    data object ClearClick : ReflectionEditorIntent

    data object CancelClick : ReflectionEditorIntent

    data object SaveClick : ReflectionEditorIntent
}
