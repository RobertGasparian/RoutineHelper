package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteEditorUiStateTest {
    @Test
    fun `given daily item note when editor state is created then uses daily note copy`() {
        val state = NoteEditorUiState.item(
            routineItemId = 10L,
            note = "",
            cadence = RoutineCadence.Daily,
            itemTitle = "Drink water",
        )

        assertEquals(RoutineCadence.Daily, state.cadence)
        assertEquals("Drink water", state.itemTitle)
        assertEquals("", state.value.text)
        assertEquals(NoteEditorTarget.Item(routineItemId = 10L), state.target)
    }

    @Test
    fun `given weekly item note when editor state is created then uses weekly note copy`() {
        val state = NoteEditorUiState.item(
            routineItemId = 20L,
            note = "Stretch after work.",
            cadence = RoutineCadence.Weekly,
            itemTitle = "Stretch",
        )

        assertEquals(RoutineCadence.Weekly, state.cadence)
        assertEquals("Stretch", state.itemTitle)
        assertEquals("Stretch after work.", state.value.text)
        assertEquals(NoteEditorTarget.Item(routineItemId = 20L), state.target)
    }

    @Test
    fun `given daily summary note when editor state is created then uses day summary copy`() {
        val state = NoteEditorUiState.summary(
            note = "Steady day.",
            cadence = RoutineCadence.Daily,
        )

        assertEquals(RoutineCadence.Daily, state.cadence)
        assertEquals(null, state.itemTitle)
        assertEquals("Steady day.", state.value.text)
        assertEquals(NoteEditorTarget.Summary, state.target)
    }

    @Test
    fun `given weekly summary note when editor state is created then uses week summary copy`() {
        val state = NoteEditorUiState.summary(
            note = "Good week.",
            cadence = RoutineCadence.Weekly,
        )

        assertEquals(RoutineCadence.Weekly, state.cadence)
        assertEquals(null, state.itemTitle)
        assertEquals("Good week.", state.value.text)
        assertEquals(NoteEditorTarget.Summary, state.target)
    }
}
