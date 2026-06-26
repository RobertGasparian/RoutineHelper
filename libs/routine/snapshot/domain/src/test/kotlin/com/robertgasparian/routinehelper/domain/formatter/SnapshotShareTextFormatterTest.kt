package com.robertgasparian.routinehelper.domain.formatter

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotShareTextFormatterTest {
    private val formatter = SnapshotShareTextFormatter(FixedTimeProvider())

    @Test
    fun `given populated snapshot when formatting share text then human readable text is returned`() {
        val text = formatter(
            RoutineSnapshot(
                snapshotId = 1L,
                periodStartDate = "2026-05-29",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineSnapshotItem(
                        actionId = 101L,
                        title = "Stretch",
                        description = null,
                        position = 1,
                        isChecked = false,
                        note = null,
                        repeatTargetCount = 3,
                        completedCount = 1,
                    ),
                    RoutineSnapshotItem(
                        actionId = 100L,
                        title = "Drink water",
                        description = "Drink 3L water",
                        position = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                ),
            ),
        )

        assertTrue(text.contains("Daily routine snapshot"))
        assertTrue(text.contains("Date: 2026-05-29"))
        assertTrue(text.contains("Finalized: 10:30 AM"))
        assertTrue(text.contains("Summary note:"))
        assertTrue(text.contains("Low-energy day, but I kept the basics moving."))
        assertTrue(text.contains("1. [x] Drink water"))
        assertTrue(text.contains("   Description: Drink 3L water"))
        assertTrue(text.contains("   Note: One liter was diet soda."))
        assertTrue(text.contains("2. [ ] Stretch"))
        assertTrue(text.contains("   Count: 1/3"))
        assertTrue(text.indexOf("Drink water") < text.indexOf("Stretch"))
    }

    @Test
    fun `given empty daily snapshot when formatting share text then daily empty state text is returned`() {
        val text = formatter(
            RoutineSnapshot(
                snapshotId = 1L,
                periodStartDate = "2026-05-29",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = RoutineCadence.Daily,
                items = emptyList(),
            ),
        )

        assertTrue(text.contains("No actions were saved for this day."))
        assertFalse(text.endsWith("\n"))
    }

    @Test
    fun `given empty weekly snapshot when formatting share text then weekly empty state text is returned`() {
        val text = formatter(
            RoutineSnapshot(
                snapshotId = 1L,
                periodStartDate = "2026-05-25",
                finalizedAtMillis = FINALIZED_AT_MILLIS,
                cadence = RoutineCadence.Weekly,
                items = emptyList(),
            ),
        )

        assertTrue(text.contains("Weekly routine snapshot"))
        assertTrue(text.contains("Week of: 2026-05-25"))
        assertTrue(text.contains("No actions were saved for this week."))
        assertFalse(text.contains("No actions were saved for this day."))
    }

    @Test
    fun `given multiple snapshots when formatting share text then newest snapshot is first`() {
        val text = formatter(
            listOf(
                RoutineSnapshot(
                    snapshotId = 1L,
                    periodStartDate = "2026-05-28",
                    finalizedAtMillis = FINALIZED_AT_MILLIS,
                    items = emptyList(),
                ),
                RoutineSnapshot(
                    snapshotId = 2L,
                    periodStartDate = "2026-05-29",
                    finalizedAtMillis = FINALIZED_AT_MILLIS,
                    items = emptyList(),
                ),
            ),
        )

        assertTrue(text.indexOf("Date: 2026-05-29") < text.indexOf("Date: 2026-05-28"))
        assertTrue(text.contains("---"))
    }

    private companion object {
        val FINALIZED_AT_MILLIS: Long = Instant.parse("2026-05-29T14:30:00Z").toEpochMilli()
    }
}
