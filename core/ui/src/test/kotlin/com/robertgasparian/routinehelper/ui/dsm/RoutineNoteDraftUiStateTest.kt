package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineNoteDraftUiStateTest {
    @Test
    fun `given note draft selection when mapped to text field value then text and selection are preserved`() {
        val textFieldValue = RoutineNoteDraftUiState(
            text = "Steady progress",
            selectionStart = 2,
            selectionEnd = 8,
        ).toTextFieldValue()

        assertEquals("Steady progress", textFieldValue.text)
        assertEquals(TextRange(2, 8), textFieldValue.selection)
    }

    @Test
    fun `given out of bounds note draft selection when mapped to text field value then selection is clamped`() {
        val textFieldValue = RoutineNoteDraftUiState(
            text = "Steady",
            selectionStart = -4,
            selectionEnd = 99,
        ).toTextFieldValue()

        assertEquals(TextRange(0, 6), textFieldValue.selection)
    }

    @Test
    fun `given text field value when mapped to note draft then text and selection are preserved`() {
        val draft = TextFieldValue(
            text = "Steady progress",
            selection = TextRange(3, 9),
        ).toRoutineNoteDraftUiState()

        assertEquals(
            RoutineNoteDraftUiState(
                text = "Steady progress",
                selectionStart = 3,
                selectionEnd = 9,
            ),
            draft,
        )
    }

    @Test
    fun `given selected text when inserting then selection is replaced and cursor follows inserted text`() {
        val updatedDraft = RoutineNoteDraftUiState(
            text = "Good slow day",
            selectionStart = 5,
            selectionEnd = 9,
        ).insertAtCursor("steady")

        assertEquals(
            RoutineNoteDraftUiState(
                text = "Good steady day",
                selectionStart = 11,
                selectionEnd = 11,
            ),
            updatedDraft,
        )
    }
}
