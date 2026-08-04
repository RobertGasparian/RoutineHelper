package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorDraftTag
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSaveRequest
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorSession
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorState
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReflectionEditorSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val tagInputNormalizer: ReflectionTagInputNormalizer,
) : BaseViewModel<ReflectionEditorState, ReflectionEditorIntent, Nothing>(), ReflectionEditorSession {
    private val mutableState = MutableStateFlow(savedStateHandle.restoreState())
    override val uiState: StateFlow<ReflectionEditorState> = mutableState.asStateFlow()
    override val state: StateFlow<ReflectionEditorState> = uiState

    override fun handleIntent(intent: ReflectionEditorIntent) {
        when (intent) {
            is ReflectionEditorIntent.DraftChange -> updateDraft(
                text = intent.text,
                selectionStart = intent.selectionStart,
                selectionEnd = intent.selectionEnd,
            )
            is ReflectionEditorIntent.RatingChange -> updateRating(intent.rating)
            is ReflectionEditorIntent.TagSelectionChange -> toggleTagSelection(intent.draftId)
            is ReflectionEditorIntent.AddTag -> addTag(intent.label)
            is ReflectionEditorIntent.DeleteTag -> deleteTag(intent.draftId)
            ReflectionEditorIntent.ClearClick -> clearDraft()
            ReflectionEditorIntent.CancelClick -> cancel()
            ReflectionEditorIntent.SaveClick -> requestSave()
        }
    }

    override fun start(initialState: ReflectionEditorInitialState) {
        val initialTags = initialState.tags.mapIndexed { index, tag ->
            ReflectionEditorDraftTag(
                draftId = index.toLong() + 1L,
                sourceId = tag.sourceId,
                label = tag.label,
                isSelected = tag.isSelected,
            )
        }
        savedStateHandle[NextDraftTagIdKey] = initialTags.size.toLong() + 1L
        updateState(
            ReflectionEditorState(
                isInitialized = true,
                originalText = initialState.text,
                originalRating = initialState.rating,
                originalTags = initialTags,
                draftText = initialState.text,
                draftRating = initialState.rating,
                draftTags = initialTags,
                selectionStart = initialState.text.length,
                selectionEnd = initialState.text.length,
            ),
        )
    }

    private fun updateDraft(
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

    private fun clearDraft() {
        if (!state.value.isInitialized) return

        updateState(
            state.value.copy(
                draftText = "",
                selectionStart = 0,
                selectionEnd = 0,
            ),
        )
    }

    private fun updateRating(rating: ReflectionRating?) {
        if (!state.value.isInitialized) return

        updateState(state.value.copy(draftRating = rating))
    }

    private fun toggleTagSelection(draftId: Long) {
        if (!state.value.isInitialized) return

        updateState(
            state.value.copy(
                draftTags = state.value.draftTags.map { tag ->
                    if (tag.draftId == draftId) {
                        tag.copy(isSelected = !tag.isSelected)
                    } else {
                        tag
                    }
                },
            ),
        )
    }

    private fun addTag(label: String) {
        if (!state.value.isInitialized) return

        val normalizedLabel = runCatching {
            tagInputNormalizer.normalizeLabel(label)
        }.getOrNull() ?: return
        val normalizedKey = tagInputNormalizer.normalizedKey(normalizedLabel)
        val existingTag = state.value.draftTags.firstOrNull { tag ->
            tagInputNormalizer.normalizedKey(tag.label) == normalizedKey
        }
        if (existingTag != null) {
            updateState(
                state.value.copy(
                    draftTags = state.value.draftTags.map { tag ->
                        if (tag.draftId == existingTag.draftId) {
                            tag.copy(isSelected = true)
                        } else {
                            tag
                        }
                    },
                ),
            )
            return
        }

        val draftId = savedStateHandle.get<Long>(NextDraftTagIdKey) ?: 1L
        savedStateHandle[NextDraftTagIdKey] = draftId + 1L
        updateState(
            state.value.copy(
                draftTags = state.value.draftTags + ReflectionEditorDraftTag(
                    draftId = draftId,
                    label = normalizedLabel,
                    isSelected = true,
                ),
            ),
        )
    }

    private fun deleteTag(draftId: Long) {
        if (!state.value.isInitialized) return

        updateState(
            state.value.copy(
                draftTags = state.value.draftTags.filterNot { tag -> tag.draftId == draftId },
            ),
        )
    }

    private fun requestSave() {
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
                    originalTags = currentState.originalTags.map(ReflectionEditorDraftTag::toEditorTag),
                    tags = currentState.draftTags.map(ReflectionEditorDraftTag::toEditorTag),
                ),
            ),
        )
    }

    override fun consumeSaveRequest(requestId: Long) {
        if (state.value.saveRequest?.requestId == requestId) {
            updateState(ReflectionEditorState())
        }
    }

    private fun cancel() {
        updateState(ReflectionEditorState())
    }

    private fun updateState(updatedState: ReflectionEditorState) {
        mutableState.value = updatedState
        savedStateHandle[IsInitializedKey] = updatedState.isInitialized
        savedStateHandle[OriginalTextKey] = updatedState.originalText
        savedStateHandle[OriginalRatingKey] = updatedState.originalRating?.value
        savedStateHandle.saveTags(OriginalTagsKey, updatedState.originalTags)
        savedStateHandle[DraftTextKey] = updatedState.draftText
        savedStateHandle[DraftRatingKey] = updatedState.draftRating?.value
        savedStateHandle.saveTags(DraftTagsKey, updatedState.draftTags)
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
            originalTags = restoreTags(OriginalTagsKey),
            draftText = get<String>(DraftTextKey).orEmpty(),
            draftRating = get<Int>(DraftRatingKey)?.let(::ReflectionRating),
            draftTags = restoreTags(DraftTagsKey),
            selectionStart = get<Int>(SelectionStartKey) ?: 0,
            selectionEnd = get<Int>(SelectionEndKey) ?: 0,
            saveRequest = if (requestId != null && requestText != null) {
                ReflectionEditorSaveRequest(
                    requestId = requestId,
                    text = requestText,
                    rating = get<Int>(SaveRequestRatingKey)?.let(::ReflectionRating),
                    originalTags = restoreTags(OriginalTagsKey).map(ReflectionEditorDraftTag::toEditorTag),
                    tags = restoreTags(DraftTagsKey).map(ReflectionEditorDraftTag::toEditorTag),
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
        const val OriginalTagsKey = "reflection.originalTags"
        const val DraftTextKey = "reflection.draftText"
        const val DraftRatingKey = "reflection.draftRating"
        const val DraftTagsKey = "reflection.draftTags"
        const val SelectionStartKey = "reflection.selectionStart"
        const val SelectionEndKey = "reflection.selectionEnd"
        const val SaveRequestIdKey = "reflection.saveRequestId"
        const val SaveRequestTextKey = "reflection.saveRequestText"
        const val SaveRequestRatingKey = "reflection.saveRequestRating"
        const val NextRequestIdKey = "reflection.nextRequestId"
        const val NextDraftTagIdKey = "reflection.nextDraftTagId"
    }
}

private fun ReflectionEditorDraftTag.toEditorTag(): ReflectionEditorTag =
    ReflectionEditorTag(
        sourceId = sourceId,
        label = label,
        isSelected = isSelected,
    )

private fun SavedStateHandle.saveTags(
    key: String,
    tags: List<ReflectionEditorDraftTag>,
) {
    this["$key.draftIds"] = tags.map(ReflectionEditorDraftTag::draftId).toLongArray()
    this["$key.sourceIds"] = tags.map { tag -> tag.sourceId ?: MissingSourceId }.toLongArray()
    this["$key.labels"] = ArrayList(tags.map(ReflectionEditorDraftTag::label))
    this["$key.selected"] = tags.map(ReflectionEditorDraftTag::isSelected).toBooleanArray()
}

private fun SavedStateHandle.restoreTags(key: String): List<ReflectionEditorDraftTag> {
    val draftIds = get<LongArray>("$key.draftIds") ?: return emptyList()
    val sourceIds = get<LongArray>("$key.sourceIds") ?: return emptyList()
    val labels = get<ArrayList<String>>("$key.labels") ?: return emptyList()
    val selected = get<BooleanArray>("$key.selected") ?: return emptyList()
    val size = minOf(draftIds.size, sourceIds.size, labels.size, selected.size)
    return List(size) { index ->
        ReflectionEditorDraftTag(
            draftId = draftIds[index],
            sourceId = sourceIds[index].takeUnless { it == MissingSourceId },
            label = labels[index],
            isSelected = selected[index],
        )
    }
}

private const val MissingSourceId = Long.MIN_VALUE

private fun Long?.orZero(): Long = this ?: 0L
