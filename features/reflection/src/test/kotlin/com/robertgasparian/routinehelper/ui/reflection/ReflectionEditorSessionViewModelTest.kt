package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorInitialState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionEditorSessionViewModelTest {
    @Test
    fun `given initial text when session starts then draft is initialized at the end`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())

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
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(2)))
        viewModel.updateDraft("Updated", selectionStart = 7)
        viewModel.updateRating(ReflectionRating(5))

        viewModel.requestSave()
        viewModel.requestSave()

        assertEquals(1L, viewModel.state.value.saveRequest?.requestId)
        assertEquals("Updated", viewModel.state.value.saveRequest?.text)
        assertEquals(ReflectionRating(5), viewModel.state.value.saveRequest?.rating)
    }

    @Test
    fun `given matching save request when consumed then session is reset`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = null))
        viewModel.requestSave()

        viewModel.consumeSaveRequest(requestId = 1L)

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given edited draft when canceled then no save request is emitted`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(3)))
        viewModel.updateDraft("Unsaved", selectionStart = 7)

        viewModel.cancel()

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given unsaved draft when a new session starts then new initial text replaces it`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start(ReflectionEditorInitialState(text = "First", rating = ReflectionRating(1)))
        viewModel.updateDraft("Unsaved", selectionStart = 7)
        viewModel.updateRating(ReflectionRating(5))

        viewModel.start(ReflectionEditorInitialState(text = "Second", rating = ReflectionRating(2)))

        assertEquals("Second", viewModel.state.value.originalText)
        assertEquals("Second", viewModel.state.value.draftText)
        assertEquals(ReflectionRating(2), viewModel.state.value.draftRating)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given saved state when ViewModel is recreated then draft and request are restored`() {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = ReflectionEditorSessionViewModel(savedStateHandle)
        firstViewModel.start(ReflectionEditorInitialState(text = "Initial", rating = ReflectionRating(2)))
        firstViewModel.updateDraft("Restored", selectionStart = 8)
        firstViewModel.updateRating(ReflectionRating(4))
        firstViewModel.requestSave()

        val restoredViewModel = ReflectionEditorSessionViewModel(savedStateHandle)

        assertEquals("Restored", restoredViewModel.state.value.draftText)
        assertEquals("Restored", restoredViewModel.state.value.saveRequest?.text)
        assertEquals(ReflectionRating(4), restoredViewModel.state.value.draftRating)
        assertEquals(ReflectionRating(4), restoredViewModel.state.value.saveRequest?.rating)
        assertEquals(1L, restoredViewModel.state.value.saveRequest?.requestId)
    }
}
