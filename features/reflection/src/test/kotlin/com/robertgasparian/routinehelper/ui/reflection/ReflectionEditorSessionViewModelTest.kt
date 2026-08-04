package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionEditorSessionViewModelTest {
    @Test
    fun `given initial text when session starts then draft is initialized at the end`() {
        val viewModel = viewModel()

        viewModel.start(
            ReflectionEditorInitialState(
                text = "A good day",
                rating = ReflectionRating(4),
            ),
        )

        assertEquals("A good day", viewModel.state.value.originalText)
        assertEquals("A good day", viewModel.state.value.draftText)
        assertEquals(ReflectionRating(4), viewModel.state.value.draftRating)
        assertEquals(10, viewModel.state.value.selectionStart)
        assertTrue(viewModel.state.value.isInitialized)
    }

    @Test
    fun `given edited draft when save is requested then client receives one request`() {
        val viewModel = viewModel()
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(2)))
        viewModel.onIntent(
            ReflectionEditorIntent.DraftChange(
                text = "Updated",
                selectionStart = 7,
                selectionEnd = 7,
            ),
        )
        viewModel.onIntent(ReflectionEditorIntent.RatingChange(ReflectionRating(5)))

        viewModel.onIntent(ReflectionEditorIntent.SaveClick)
        viewModel.onIntent(ReflectionEditorIntent.SaveClick)

        assertEquals(1L, viewModel.state.value.saveRequest?.requestId)
        assertEquals("Updated", viewModel.state.value.saveRequest?.text)
        assertEquals(ReflectionRating(5), viewModel.state.value.saveRequest?.rating)
    }

    @Test
    fun `given matching save request when consumed then session is reset`() {
        val viewModel = viewModel()
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = null))
        viewModel.onIntent(ReflectionEditorIntent.SaveClick)

        viewModel.consumeSaveRequest(requestId = 1L)

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given edited draft when canceled then no save request is emitted`() {
        val viewModel = viewModel()
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(3)))
        viewModel.onIntent(
            ReflectionEditorIntent.DraftChange(
                text = "Unsaved",
                selectionStart = 7,
                selectionEnd = 7,
            ),
        )

        viewModel.onIntent(ReflectionEditorIntent.CancelClick)

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given unsaved draft when a new session starts then new initial text replaces it`() {
        val viewModel = viewModel()
        viewModel.start(ReflectionEditorInitialState(text = "First", rating = ReflectionRating(1)))
        viewModel.onIntent(
            ReflectionEditorIntent.DraftChange(
                text = "Unsaved",
                selectionStart = 7,
                selectionEnd = 7,
            ),
        )
        viewModel.onIntent(ReflectionEditorIntent.RatingChange(ReflectionRating(5)))

        viewModel.start(ReflectionEditorInitialState(text = "Second", rating = ReflectionRating(2)))

        assertEquals("Second", viewModel.state.value.originalText)
        assertEquals("Second", viewModel.state.value.draftText)
        assertEquals(ReflectionRating(2), viewModel.state.value.draftRating)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given saved state when ViewModel is recreated then draft and request are restored`() {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = viewModel(savedStateHandle)
        firstViewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(2)))
        firstViewModel.onIntent(
            ReflectionEditorIntent.DraftChange(
                text = "Restored",
                selectionStart = 8,
                selectionEnd = 8,
            ),
        )
        firstViewModel.onIntent(ReflectionEditorIntent.RatingChange(ReflectionRating(4)))
        firstViewModel.onIntent(ReflectionEditorIntent.SaveClick)

        val restoredViewModel = viewModel(savedStateHandle)

        assertEquals("Restored", restoredViewModel.state.value.draftText)
        assertEquals("Restored", restoredViewModel.state.value.saveRequest?.text)
        assertEquals(ReflectionRating(4), restoredViewModel.state.value.draftRating)
        assertEquals(ReflectionRating(4), restoredViewModel.state.value.saveRequest?.rating)
        assertEquals(1L, restoredViewModel.state.value.saveRequest?.requestId)
    }

    @Test
    fun `given tag draft when edited then save returns original and current values`() {
        val viewModel = viewModel()
        viewModel.start(
            ReflectionEditorInitialState(
                text = "",
                rating = null,
                tags = listOf(
                    ReflectionEditorTag(sourceId = 7L, label = "Calm", isSelected = false),
                ),
            ),
        )
        val calmDraftId = viewModel.state.value.draftTags.single().draftId

        viewModel.onIntent(ReflectionEditorIntent.TagSelectionChange(calmDraftId))
        viewModel.onIntent(ReflectionEditorIntent.AddTag("  Productive   day "))
        viewModel.onIntent(ReflectionEditorIntent.SaveClick)

        assertEquals(
            listOf(ReflectionEditorTag(sourceId = 7L, label = "Calm", isSelected = false)),
            viewModel.state.value.saveRequest?.originalTags,
        )
        assertEquals(
            listOf(
                ReflectionEditorTag(sourceId = 7L, label = "Calm", isSelected = true),
                ReflectionEditorTag(label = "Productive day", isSelected = true),
            ),
            viewModel.state.value.saveRequest?.tags,
        )
    }

    @Test
    fun `given duplicate tag label when added then existing tag becomes selected`() {
        val viewModel = viewModel()
        viewModel.start(
            ReflectionEditorInitialState(
                text = "",
                rating = null,
                tags = listOf(
                    ReflectionEditorTag(sourceId = 7L, label = "Calm", isSelected = false),
                ),
            ),
        )

        viewModel.onIntent(ReflectionEditorIntent.AddTag(" calm "))

        assertEquals(1, viewModel.state.value.draftTags.size)
        assertTrue(viewModel.state.value.draftTags.single().isSelected)
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        ReflectionEditorSessionViewModel(
            savedStateHandle = savedStateHandle,
            tagInputNormalizer = ReflectionTagInputNormalizer(),
        )
}
