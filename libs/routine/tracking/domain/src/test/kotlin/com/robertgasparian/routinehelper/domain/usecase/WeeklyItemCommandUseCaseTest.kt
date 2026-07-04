package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.WeeklyCheckedChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyCountChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyHiddenChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyNoteChange
import com.robertgasparian.routinehelper.domain.repository.WeeklySummaryNoteChange
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyItemCommandUseCaseTest {
    private val repository = FakeWeeklyRoutineRepository()

    @Test
    fun `given checked state when updating weekly item then repository receives the change`() = runTest {
        SetWeeklyItemCheckedUseCase(repository)(
            weekStartDate = "2026-05-24",
            routineItemId = 10L,
            isChecked = true,
        )

        assertEquals(
            listOf(
                WeeklyCheckedChange(
                    weekStartDate = "2026-05-24",
                    routineItemId = 10L,
                    isChecked = true,
                ),
            ),
            repository.checkedChanges,
        )
    }

    @Test
    fun `given hidden state when updating weekly item then repository receives the change`() = runTest {
        SetWeeklyItemHiddenUseCase(repository)(
            weekStartDate = "2026-05-24",
            routineItemId = 10L,
            isHidden = true,
        )

        assertEquals(
            listOf(
                WeeklyHiddenChange(
                    weekStartDate = "2026-05-24",
                    routineItemId = 10L,
                    isHidden = true,
                ),
            ),
            repository.hiddenChanges,
        )
    }

    @Test
    fun `given note when updating weekly item then repository receives the change`() = runTest {
        UpdateWeeklyItemNoteUseCase(repository)(
            weekStartDate = "2026-05-24",
            routineItemId = 10L,
            note = "Done on Friday",
        )

        assertEquals(
            listOf(
                WeeklyNoteChange(
                    weekStartDate = "2026-05-24",
                    routineItemId = 10L,
                    note = "Done on Friday",
                ),
            ),
            repository.noteChanges,
        )
    }

    @Test
    fun `given completed count when updating weekly item then repository receives the change`() = runTest {
        UpdateWeeklyItemCompletedCountUseCase(repository)(
            weekStartDate = "2026-05-24",
            routineItemId = 10L,
            completedCount = 2,
        )

        assertEquals(
            listOf(
                WeeklyCountChange(
                    weekStartDate = "2026-05-24",
                    routineItemId = 10L,
                    completedCount = 2,
                ),
            ),
            repository.countChanges,
        )
    }

    @Test
    fun `given summary note when updating week then repository receives the change`() = runTest {
        UpdateWeeklySummaryNoteUseCase(repository)(
            weekStartDate = "2026-05-24",
            note = "Kept most weekly priorities moving.",
        )

        assertEquals(
            listOf(
                WeeklySummaryNoteChange(
                    weekStartDate = "2026-05-24",
                    note = "Kept most weekly priorities moving.",
                ),
            ),
            repository.summaryNoteChanges,
        )
    }
}
