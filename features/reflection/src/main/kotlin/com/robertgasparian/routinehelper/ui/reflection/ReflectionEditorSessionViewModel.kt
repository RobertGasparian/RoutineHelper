package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
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

    override fun start(initialState: ReflectionEditorInitialState) {
        updateState(
            ReflectionEditorState(
                isInitialized = true,
                originalText = initialState.text,
                originalRating = initialState.rating,
                draftText = initialState.text,
                draftRating = initialState.rating,
                selectionStart = initialState.text.length,
                selectionEnd = initialState.text.length,
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

    internal fun updateRating(rating: ReflectionRating?) {
        if (!state.value.isInitialized) return

        updateState(state.value.copy(draftRating = rating))
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
                    rating = currentState.draftRating,
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
        savedStateHandle[OriginalRatingKey] = updatedState.originalRating?.value
        savedStateHandle[DraftTextKey] = updatedState.draftText
        savedStateHandle[DraftRatingKey] = updatedState.draftRating?.value
        savedStateHandle[SelectionStartKey] = updatedState.selectionStart
        savedStateHandle[SelectionEndKey] = updatedState.selectionEnd
        savedStateHandle[SaveRequestIdKey] = updatedState.saveRequest?.requestId
        savedStateHandle[SaveRequestTextKey] = updatedState.saveRequest?.text
        savedStateHandle[SaveRequestRatingKey] = updatedState.saveRequest?.rating?.value
    }

    private fun SavedStateHandle.restoreState(): ReflectionEditorState {
        val requestId = get<Long>(SaveRequestIdKey)
        val requestText = get<String>(SaveRequestTextKey)
        return ReflectionEditorState(
            isInitialized = get<Boolean>(IsInitializedKey) ?: false,
            originalText = get<String>(OriginalTextKey).orEmpty(),
            originalRating = get<Int>(OriginalRatingKey)?.let(::ReflectionRating),
            draftText = get<String>(DraftTextKey).orEmpty(),
            draftRating = get<Int>(DraftRatingKey)?.let(::ReflectionRating),
            selectionStart = get<Int>(SelectionStartKey) ?: 0,
            selectionEnd = get<Int>(SelectionEndKey) ?: 0,
            saveRequest = if (requestId != null && requestText != null) {
                ReflectionEditorSaveRequest(
                    requestId = requestId,
                    text = requestText,
                    rating = get<Int>(SaveRequestRatingKey)?.let(::ReflectionRating),
                )
            } else {
                null
            },
        )
    }

    private companion object {
        const val IsInitializedKey = "reflection.isInitialized"
        const val OriginalTextKey = "reflection.originalText"
        const val OriginalRatingKey = "reflection.originalRating"
        const val DraftTextKey = "reflection.draftText"
        const val DraftRatingKey = "reflection.draftRating"
        const val SelectionStartKey = "reflection.selectionStart"
        const val SelectionEndKey = "reflection.selectionEnd"
        const val SaveRequestIdKey = "reflection.saveRequestId"
        const val SaveRequestTextKey = "reflection.saveRequestText"
        const val SaveRequestRatingKey = "reflection.saveRequestRating"
        const val NextRequestIdKey = "reflection.nextRequestId"
    }
}

private fun Long?.orZero(): Long = this ?: 0L
