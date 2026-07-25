package com.robertgasparian.routinehelper.ui.tracking

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDraftComposeMappersTest {
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
