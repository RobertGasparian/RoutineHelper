package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotShareTextUseCaseTest {
    private val useCase = SnapshotShareTextUseCase()

    @Test
    fun formatsSnapshotForHumanReading() {
        val text = useCase(
            RoutineDaySnapshot(
                snapshotId = 1L,
                date = "2026-05-29",
                finalizedAtMillis = 1_234L,
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 101L,
                        title = "Stretch",
                        description = null,
                        position = 1,
                        isChecked = false,
                        note = null,
                    ),
                    RoutineDaySnapshotItem(
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

        assertTrue(text.contains("Routine snapshot"))
        assertTrue(text.contains("Date: 2026-05-29"))
        assertTrue(text.contains("1. [x] Drink water"))
        assertTrue(text.contains("   Description: Drink 3L water"))
        assertTrue(text.contains("   Note: One liter was diet soda."))
        assertTrue(text.contains("2. [ ] Stretch"))
        assertTrue(text.indexOf("Drink water") < text.indexOf("Stretch"))
    }

    @Test
    fun formatsEmptySnapshot() {
        val text = useCase(
            RoutineDaySnapshot(
                snapshotId = 1L,
                date = "2026-05-29",
                finalizedAtMillis = 1_234L,
                items = emptyList(),
            ),
        )

        assertTrue(text.contains("No actions were saved for this day."))
        assertFalse(text.endsWith("\n"))
    }

    @Test
    fun formatsMultipleSnapshotsNewestFirst() {
        val text = useCase(
            listOf(
                RoutineDaySnapshot(
                    snapshotId = 1L,
                    date = "2026-05-28",
                    finalizedAtMillis = 1_234L,
                    items = emptyList(),
                ),
                RoutineDaySnapshot(
                    snapshotId = 2L,
                    date = "2026-05-29",
                    finalizedAtMillis = 1_234L,
                    items = emptyList(),
                ),
            ),
        )

        assertTrue(text.indexOf("Date: 2026-05-29") < text.indexOf("Date: 2026-05-28"))
        assertTrue(text.contains("---"))
    }
}
