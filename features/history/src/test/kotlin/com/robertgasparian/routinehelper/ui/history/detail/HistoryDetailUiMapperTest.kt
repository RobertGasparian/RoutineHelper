package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDetailUiMapperTest {
    @Test
    fun `given weekly snapshot when mapped then detail fields are preserved`() {
        val state = RoutineSnapshot(
            snapshotId = 1L,
            periodStartDate = "2026-05-25",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = RoutineCadence.Weekly,
            summaryNote = "Good week",
            items = listOf(
                RoutineSnapshotItem(
                    actionId = 100L,
                    title = "Review goals",
                    description = "Review the weekly plan",
                    position = 0,
                    isChecked = false,
                    note = "Almost complete",
                    repeatTargetCount = 3,
                    completedCount = 2,
                ),
                RoutineSnapshotItem(
                    actionId = 101L,
                    title = "Old task",
                    description = null,
                    position = 1,
                    isChecked = false,
                    isHidden = true,
                    note = null,
                ),
            ),
        ).toHistoryDetailUiState(finalizedTime = "12:00 PM")

        assertEquals(
            HistoryDetailUiState(
                date = "2026-05-25",
                cadence = RoutineCadence.Weekly,
                finalizedTime = "12:00 PM",
                summaryNote = "Good week",
                items = listOf(
                    HistoryDetailItemUiState(
                        actionId = 100L,
                        title = "Review goals",
                        description = "Review the weekly plan",
                        repeatTargetCount = 3,
                        completedCount = 2,
                        isChecked = false,
                        note = "Almost complete",
                    ),
                    HistoryDetailItemUiState(
                        actionId = 101L,
                        title = "Old task",
                        description = null,
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = false,
                        isHidden = true,
                        note = null,
                    ),
                ),
            ),
            state,
        )
    }

    @Test
    fun `given daily snapshot without summary note when mapped then date is not prefixed and summary is empty`() {
        val snapshot = RoutineSnapshot(
            snapshotId = 1L,
            periodStartDate = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = RoutineCadence.Daily,
            summaryNote = null,
            isSummaryNoteEditable = false,
            items = emptyList(),
        )

        val state = snapshot.toHistoryDetailUiState(finalizedTime = "12:00 PM")

        assertEquals("2026-05-29", state.date)
        assertEquals("", state.summaryNote)
        assertEquals(false, state.isSummaryNoteEditable)
        assertEquals("2026-05-29", snapshot.historyDisplayDate)
    }

    private companion object {
        val FINALIZED_AT_MILLIS: Long = Instant.parse("2026-05-29T16:00:00Z").toEpochMilli()
    }
}
