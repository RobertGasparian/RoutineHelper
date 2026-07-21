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
    fun `given history filter when mapped to label resource then matching resource is returned`() {
        assertEquals(com.robertgasparian.routinehelper.features.history.R.string.history_filter_all, HistoryFilter.All.labelRes)
        assertEquals(com.robertgasparian.routinehelper.features.history.R.string.history_cadence_daily, HistoryFilter.Daily.labelRes)
        assertEquals(com.robertgasparian.routinehelper.features.history.R.string.history_cadence_weekly, HistoryFilter.Weekly.labelRes)
    }
}
