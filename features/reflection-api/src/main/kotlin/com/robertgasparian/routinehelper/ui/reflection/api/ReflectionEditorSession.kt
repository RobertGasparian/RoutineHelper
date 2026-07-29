package com.robertgasparian.routinehelper.ui.reflection.api

import kotlinx.coroutines.flow.StateFlow

/**
 * State shared by a Reflection editor and the client entry that opened it.
 *
 * The client supplies initial state and remains responsible for persisting [ReflectionEditorSaveRequest].
 * Reflection owns only the draft and explicit-save interaction.
 */
interface ReflectionEditorSession {
    val state: StateFlow<ReflectionEditorState>

    fun start(initialText: String)

    fun consumeSaveRequest(requestId: Long)
}

data class ReflectionEditorState(
    val isInitialized: Boolean = false,
    val originalText: String = "",
    val draftText: String = "",
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
                draftText = "A steady day with good progress.",
                selectionStart = 32,
                selectionEnd = 32,
            )
    }
}

data class ReflectionEditorSaveRequest(
    val requestId: Long,
    val text: String,
)
