package com.robertgasparian.routinehelper.ui.reflection.api

import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import kotlinx.coroutines.flow.StateFlow

/**
 * State shared by a Reflection editor and the client entry that opened it.
 *
 * The client supplies initial state and remains responsible for persisting [ReflectionEditorSaveRequest].
 * Reflection owns only the draft and explicit-save interaction.
 */
interface ReflectionEditorSession {
    val state: StateFlow<ReflectionEditorState>

    fun start(initialState: ReflectionEditorInitialState)

    fun consumeSaveRequest(requestId: Long)
}

data class ReflectionEditorInitialState(
    val text: String,
    val rating: ReflectionRating?,
    val tags: List<ReflectionEditorTag> = emptyList(),
)

/**
 * A tag value exchanged between the Reflection editor and its client.
 *
 * [sourceId] is an opaque client-owned identifier. Reflection returns it unchanged for existing
 * tags and uses `null` for tags created in the draft, so the client can apply its own persistence
 * rules without Reflection knowing who opened it.
 */
data class ReflectionEditorTag(
    val sourceId: Long? = null,
    val label: String,
    val isSelected: Boolean,
)

data class ReflectionEditorDraftTag(
    val draftId: Long,
    val sourceId: Long? = null,
    val label: String,
    val isSelected: Boolean,
)

data class ReflectionEditorState(
    val isInitialized: Boolean = false,
    val originalText: String = "",
    val originalRating: ReflectionRating? = null,
    val originalTags: List<ReflectionEditorDraftTag> = emptyList(),
    val draftText: String = "",
    val draftRating: ReflectionRating? = null,
    val draftTags: List<ReflectionEditorDraftTag> = emptyList(),
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val saveRequest: ReflectionEditorSaveRequest? = null,
) {
    val canClear: Boolean = draftText.isNotBlank()

    companion object {
        fun preview(): ReflectionEditorState =
            ReflectionEditorState(
                isInitialized = true,
                originalText = "A steady day with good progress.",
                originalRating = ReflectionRating(4),
                originalTags = PreviewTags,
                draftText = "A steady day with good progress.",
                draftRating = ReflectionRating(4),
                draftTags = PreviewTags,
                selectionStart = 32,
                selectionEnd = 32,
            )
    }
}

data class ReflectionEditorSaveRequest(
    val requestId: Long,
    val text: String,
    val rating: ReflectionRating?,
    val originalTags: List<ReflectionEditorTag>,
    val tags: List<ReflectionEditorTag>,
)

private val PreviewTags = listOf(
    ReflectionEditorDraftTag(
        draftId = 1L,
        sourceId = 10L,
        label = "Productive",
        isSelected = true,
    ),
    ReflectionEditorDraftTag(
        draftId = 2L,
        sourceId = 11L,
        label = "Calm",
        isSelected = false,
    ),
)
