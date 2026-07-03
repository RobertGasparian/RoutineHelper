package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFilterTest {
    @Test
    fun `given history filter when converted to cadence then matching cadence is returned`() {
        assertEquals(null, HistoryFilter.All.snapshotCadence)
        assertEquals(RoutineCadence.Daily, HistoryFilter.Daily.snapshotCadence)
        assertEquals(RoutineCadence.Weekly, HistoryFilter.Weekly.snapshotCadence)
    }

    @Test
    fun `given history filter when mapped to label then matching label is returned`() {
        assertEquals("All", HistoryFilter.All.label)
        assertEquals("Daily", HistoryFilter.Daily.label)
        assertEquals("Weekly", HistoryFilter.Weekly.label)
    }
}
