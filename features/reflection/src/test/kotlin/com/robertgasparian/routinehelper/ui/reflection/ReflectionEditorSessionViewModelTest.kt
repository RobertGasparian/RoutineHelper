package com.robertgasparian.routinehelper.ui.reflection

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionEditorSessionViewModelTest {
    @Test
    fun `given initial text when session starts then draft is initialized at the end`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())

        viewModel.start("A good day")

        assertEquals("A good day", viewModel.state.value.originalText)
        assertEquals("A good day", viewModel.state.value.draftText)
        assertEquals(10, viewModel.state.value.selectionStart)
        assertTrue(viewModel.state.value.isInitialized)
    }

    @Test
    fun `given edited draft when save is requested then client receives one request`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start("Initial")
        viewModel.updateDraft("Updated", selectionStart = 7)

        viewModel.requestSave()
        viewModel.requestSave()

        assertEquals(1L, viewModel.state.value.saveRequest?.requestId)
        assertEquals("Updated", viewModel.state.value.saveRequest?.text)
    }

    @Test
    fun `given matching save request when consumed then session is reset`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start("Initial")
        viewModel.requestSave()

        viewModel.consumeSaveRequest(requestId = 1L)

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given edited draft when canceled then no save request is emitted`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start("Initial")
        viewModel.updateDraft("Unsaved", selectionStart = 7)

        viewModel.cancel()

        assertFalse(viewModel.state.value.isInitialized)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given unsaved draft when a new session starts then new initial text replaces it`() {
        val viewModel = ReflectionEditorSessionViewModel(SavedStateHandle())
        viewModel.start("First")
        viewModel.updateDraft("Unsaved", selectionStart = 7)

        viewModel.start("Second")

        assertEquals("Second", viewModel.state.value.originalText)
        assertEquals("Second", viewModel.state.value.draftText)
        assertNull(viewModel.state.value.saveRequest)
    }

    @Test
    fun `given saved state when ViewModel is recreated then draft and request are restored`() {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = ReflectionEditorSessionViewModel(savedStateHandle)
        firstViewModel.start("Initial")
        firstViewModel.updateDraft("Restored", selectionStart = 8)
        firstViewModel.requestSave()

        val restoredViewModel = ReflectionEditorSessionViewModel(savedStateHandle)

        assertEquals("Restored", restoredViewModel.state.value.draftText)
        assertEquals("Restored", restoredViewModel.state.value.saveRequest?.text)
        assertEquals(1L, restoredViewModel.state.value.saveRequest?.requestId)
    }
}
