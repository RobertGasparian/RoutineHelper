package com.robertgasparian.routinehelper.ui.reflection

import com.robertgasparian.routinehelper.domain.model.ReflectionRating

sealed interface ReflectionEditorIntent {
    data class DraftChange(
        val text: String,
        val selectionStart: Int,
        val selectionEnd: Int,
    ) : ReflectionEditorIntent

    data class RatingChange(
        val rating: ReflectionRating?,
    ) : ReflectionEditorIntent

    data class TagSelectionChange(
        val draftId: Long,
    ) : ReflectionEditorIntent

    data class AddTag(
        val label: String,
    ) : ReflectionEditorIntent

    data class DeleteTag(
        val draftId: Long,
    ) : ReflectionEditorIntent

    data object ClearClick : ReflectionEditorIntent

    data object CancelClick : ReflectionEditorIntent

    data object SaveClick : ReflectionEditorIntent
}
