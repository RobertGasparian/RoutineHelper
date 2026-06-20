package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotShareTextUseCaseTest {
    private val useCase = SnapshotShareTextUseCase()

    @Test
    fun `given populated snapshot when formatting share text then human readable text is returned`() {
        val text = useCase(
            RoutineDaySnapshot(
                snapshotId = 1L,
                date = "2026-05-29",
                finalizedAtMillis = 1_234L,
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    RoutineDaySnapshotItem(
                        actionId = 101L,
                        title = "Stretch",
                        description = null,
                        position = 1,
                        isChecked = false,
                        note = null,
                        repeatTargetCount = 3,
                        completedCount = 1,
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

        assertTrue(text.contains("Daily routine snapshot"))
        assertTrue(text.contains("Date: 2026-05-29"))
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
    fun `given empty snapshot when formatting share text then empty state text is returned`() {
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
    fun `given multiple snapshots when formatting share text then newest snapshot is first`() {
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
