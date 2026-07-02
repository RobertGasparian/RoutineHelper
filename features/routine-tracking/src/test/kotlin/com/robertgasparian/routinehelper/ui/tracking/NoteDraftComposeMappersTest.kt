package com.robertgasparian.routinehelper.ui.tracking

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDraftComposeMappersTest {
    @Test
    fun `given note draft selection when mapped to text field value then text and selection are preserved`() {
        val textFieldValue = NoteDraftUiState(
            text = "Steady progress",
            selectionStart = 2,
            selectionEnd = 8,
        ).toTextFieldValue()

        assertEquals("Steady progress", textFieldValue.text)
        assertEquals(TextRange(2, 8), textFieldValue.selection)
    }

    @Test
    fun `given out of bounds note draft selection when mapped to text field value then selection is clamped`() {
        val textFieldValue = NoteDraftUiState(
            text = "Steady",
            selectionStart = -4,
            selectionEnd = 99,
        ).toTextFieldValue()

        assertEquals(TextRange(0, 6), textFieldValue.selection)
    }

    @Test
    fun `given text field value when mapped to note draft change then text and selection are preserved`() {
        val intent = TextFieldValue(
            text = "Steady progress",
            selection = TextRange(3, 9),
        ).toNoteDraftChange()

        assertEquals(
            RoutineTrackingIntent.NoteDraftChange(
                text = "Steady progress",
                selectionStart = 3,
                selectionEnd = 9,
            ),
            intent,
        )
    }
}
