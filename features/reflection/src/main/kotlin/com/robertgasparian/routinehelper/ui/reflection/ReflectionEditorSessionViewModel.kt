package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSaveRequest
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSession
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReflectionEditorSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), ReflectionEditorSession {
    private val mutableState = MutableStateFlow(savedStateHandle.restoreState())
    override val state: StateFlow<ReflectionEditorState> = mutableState.asStateFlow()

    override fun start(initialText: String) {
        updateState(
            ReflectionEditorState(
                isInitialized = true,
                originalText = initialText,
                draftText = initialText,
                selectionStart = initialText.length,
                selectionEnd = initialText.length,
            ),
        )
    }

    internal fun updateDraft(
        text: String,
        selectionStart: Int,
        selectionEnd: Int = selectionStart,
    ) {
        if (!state.value.isInitialized) return

        updateState(
            state.value.copy(
                draftText = text,
                selectionStart = selectionStart.coerceIn(0, text.length),
                selectionEnd = selectionEnd.coerceIn(0, text.length),
            ),
        )
    }

    internal fun clearDraft() {
        if (!state.value.isInitialized) return

        updateState(
            state.value.copy(
                draftText = "",
                selectionStart = 0,
                selectionEnd = 0,
            ),
        )
    }

    internal fun requestSave() {
        val currentState = state.value
        if (!currentState.isInitialized || currentState.saveRequest != null) return

        val requestId = savedStateHandle.get<Long>(NextRequestIdKey).orZero() + 1L
        savedStateHandle[NextRequestIdKey] = requestId
        updateState(
            currentState.copy(
                saveRequest = ReflectionEditorSaveRequest(
                    requestId = requestId,
                    text = currentState.draftText,
                ),
            ),
        )
    }

    override fun consumeSaveRequest(requestId: Long) {
        if (state.value.saveRequest?.requestId == requestId) {
            updateState(ReflectionEditorState())
        }
    }

    internal fun cancel() {
        updateState(ReflectionEditorState())
    }

    private fun updateState(updatedState: ReflectionEditorState) {
        mutableState.value = updatedState
        savedStateHandle[IsInitializedKey] = updatedState.isInitialized
        savedStateHandle[OriginalTextKey] = updatedState.originalText
        savedStateHandle[DraftTextKey] = updatedState.draftText
        savedStateHandle[SelectionStartKey] = updatedState.selectionStart
        savedStateHandle[SelectionEndKey] = updatedState.selectionEnd
        savedStateHandle[SaveRequestIdKey] = updatedState.saveRequest?.requestId
        savedStateHandle[SaveRequestTextKey] = updatedState.saveRequest?.text
    }

    private fun SavedStateHandle.restoreState(): ReflectionEditorState {
        val requestId = get<Long>(SaveRequestIdKey)
        val requestText = get<String>(SaveRequestTextKey)
        return ReflectionEditorState(
            isInitialized = get<Boolean>(IsInitializedKey) ?: false,
            originalText = get<String>(OriginalTextKey).orEmpty(),
            draftText = get<String>(DraftTextKey).orEmpty(),
            selectionStart = get<Int>(SelectionStartKey) ?: 0,
            selectionEnd = get<Int>(SelectionEndKey) ?: 0,
            saveRequest = if (requestId != null && requestText != null) {
                ReflectionEditorSaveRequest(
                    requestId = requestId,
                    text = requestText,
                )
            } else {
                null
            },
        )
    }

    private companion object {
        const val IsInitializedKey = "reflection.isInitialized"
        const val OriginalTextKey = "reflection.originalText"
        const val DraftTextKey = "reflection.draftText"
        const val SelectionStartKey = "reflection.selectionStart"
        const val SelectionEndKey = "reflection.selectionEnd"
        const val SaveRequestIdKey = "reflection.saveRequestId"
        const val SaveRequestTextKey = "reflection.saveRequestText"
        const val NextRequestIdKey = "reflection.nextRequestId"
    }
}

private fun Long?.orZero(): Long = this ?: 0L
