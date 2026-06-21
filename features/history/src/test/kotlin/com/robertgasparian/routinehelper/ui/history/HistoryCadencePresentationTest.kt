package com.robertgasparian.routinehelper.ui.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ViewWeek
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HistoryCadencePresentationTest {
    @Test
    fun `given daily cadence when mapped for history then daily label and event icon are returned`() {
        assertEquals("Daily", RoutineCadence.Daily.historyLabel)
        assertSame(Icons.Default.Event, RoutineCadence.Daily.historyIcon)
    }

    @Test
    fun `given weekly cadence when mapped for history then weekly label and view week icon are returned`() {
        assertEquals("Weekly", RoutineCadence.Weekly.historyLabel)
        assertSame(Icons.Default.ViewWeek, RoutineCadence.Weekly.historyIcon)
    }
}
