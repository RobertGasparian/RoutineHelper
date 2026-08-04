package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorTag
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
            rating = ReflectionRating(4),
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
                rating = ReflectionRating(4),
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
            isReflectionEditable = false,
            items = emptyList(),
        )

        val state = snapshot.toHistoryDetailUiState(finalizedTime = "12:00 PM")

        assertEquals("2026-05-29", state.date)
        assertEquals("", state.summaryNote)
        assertEquals(false, state.isReflectionEditable)
        assertEquals("2026-05-29", snapshot.historyDisplayDate)
    }

    @Test
    fun `given snapshot without selected tags when mapped then current cadence template is copied unselected`() {
        val snapshot = RoutineSnapshot(
            snapshotId = 1L,
            periodStartDate = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = RoutineCadence.Daily,
            items = emptyList(),
        )

        val state = snapshot.toHistoryDetailUiState(
            finalizedTime = "12:00 PM",
            cadenceTagTemplate = listOf(
                ReflectionTagDefinition(7L, "Calm", 0, RoutineCadence.Daily),
                ReflectionTagDefinition(8L, "Productive", 1, RoutineCadence.Daily),
            ),
        )

        assertEquals(
            listOf(
                ReflectionEditorTag(label = "Calm", isSelected = false),
                ReflectionEditorTag(label = "Productive", isSelected = false),
            ),
            state.reflectionTags,
        )
    }

    @Test
    fun `given read only snapshot without selected tags when mapped then template tags are omitted`() {
        val snapshot = RoutineSnapshot(
            snapshotId = 1L,
            periodStartDate = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = RoutineCadence.Daily,
            isReflectionEditable = false,
            items = emptyList(),
        )

        val state = snapshot.toHistoryDetailUiState(
            finalizedTime = "12:00 PM",
            cadenceTagTemplate = listOf(
                ReflectionTagDefinition(7L, "Calm", 0, RoutineCadence.Daily),
            ),
        )

        assertEquals(emptyList<ReflectionEditorTag>(), state.reflectionTags)
    }

    @Test
    fun `given snapshot with selected tags when mapped then template is ignored and only snapshot tags are shown`() {
        val snapshot = RoutineSnapshot(
            snapshotId = 1L,
            periodStartDate = "2026-05-29",
            finalizedAtMillis = FINALIZED_AT_MILLIS,
            cadence = RoutineCadence.Daily,
            selectedTags = listOf(SelectedReflectionTag(label = "Snapshot only", position = 0)),
            items = emptyList(),
        )

        val state = snapshot.toHistoryDetailUiState(
            finalizedTime = "12:00 PM",
            cadenceTagTemplate = listOf(
                ReflectionTagDefinition(7L, "Current template", 0, RoutineCadence.Daily),
            ),
        )

        assertEquals(
            listOf(ReflectionEditorTag(label = "Snapshot only", isSelected = true)),
            state.reflectionTags,
        )
    }

    private companion object {
        val FINALIZED_AT_MILLIS: Long = Instant.parse("2026-05-29T16:00:00Z").toEpochMilli()
    }
}
