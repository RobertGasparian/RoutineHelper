package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySnapshotUiMapperTest {
    @Test
    fun `given daily summary when mapped then period date and selection are preserved`() {
        val uiState = summary(
            periodStartDate = "2026-05-29",
            cadence = RoutineCadence.Daily,
        ).toHistorySnapshotUiState(isSelected = true)

        assertEquals(
            HistorySnapshotUiState(
                snapshotId = 1L,
                date = "2026-05-29",
                cadence = RoutineCadence.Daily,
                completedCount = 2,
                totalCount = 3,
                hasSummaryNote = true,
                isSelected = true,
            ),
            uiState,
        )
    }

    @Test
    fun `given weekly summary when mapped then raw period date is preserved`() {
        val uiState = summary(
            periodStartDate = "2026-05-25",
            cadence = RoutineCadence.Weekly,
        ).toHistorySnapshotUiState(isSelected = false)

        assertEquals("2026-05-25", uiState.date)
    }

    private fun summary(
        periodStartDate: String,
        cadence: RoutineCadence,
    ): RoutineSnapshotSummary =
        RoutineSnapshotSummary(
            snapshotId = 1L,
            periodStartDate = periodStartDate,
            finalizedAtMillis = 1_748_534_400_000L,
            cadence = cadence,
            completedCount = 2,
            totalCount = 3,
            hasSummaryNote = true,
        )

}
