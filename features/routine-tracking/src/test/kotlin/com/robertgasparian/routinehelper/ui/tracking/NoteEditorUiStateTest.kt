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

        assertEquals("Add note", state.title)
        assertEquals("Daily note", state.label)
        assertEquals("Daily note for Drink water", state.supportingText)
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

        assertEquals("Edit note", state.title)
        assertEquals("Weekly note", state.label)
        assertEquals("Weekly note for Stretch", state.supportingText)
        assertEquals(NoteEditorTarget.Item(routineItemId = 20L), state.target)
    }

    @Test
    fun `given daily summary note when editor state is created then uses day summary copy`() {
        val state = NoteEditorUiState.summary(
            note = "Steady day.",
            cadence = RoutineCadence.Daily,
        )

        assertEquals("Day note", state.title)
        assertEquals("Day note", state.label)
        assertEquals("This note is saved for this day only.", state.supportingText)
        assertEquals(NoteEditorTarget.Summary, state.target)
    }

    @Test
    fun `given weekly summary note when editor state is created then uses week summary copy`() {
        val state = NoteEditorUiState.summary(
            note = "Good week.",
            cadence = RoutineCadence.Weekly,
        )

        assertEquals("Week note", state.title)
        assertEquals("Week note", state.label)
        assertEquals("This note is saved for the current week.", state.supportingText)
        assertEquals(NoteEditorTarget.Summary, state.target)
    }
}
