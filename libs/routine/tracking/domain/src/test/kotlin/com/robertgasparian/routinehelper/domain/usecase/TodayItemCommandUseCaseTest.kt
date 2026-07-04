package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.CheckedChange
import com.robertgasparian.routinehelper.domain.repository.CountChange
import com.robertgasparian.routinehelper.domain.repository.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.HiddenChange
import com.robertgasparian.routinehelper.domain.repository.NoteChange
import com.robertgasparian.routinehelper.domain.repository.SummaryNoteChange
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayItemCommandUseCaseTest {
    private val repository = FakeTodayRoutineRepository()

    @Test
    fun `given checked state when updating today item then repository receives the change`() = runTest {
        SetTodayItemCheckedUseCase(repository)(
            date = "2026-05-29",
            routineItemId = 42L,
            isChecked = true,
        )

        assertEquals(
            CheckedChange(
                date = "2026-05-29",
                routineItemId = 42L,
                isChecked = true,
            ),
            repository.checkedChanges.single(),
        )
    }

    @Test
    fun `given hidden state when updating today item then repository receives the change`() = runTest {
        SetTodayItemHiddenUseCase(repository)(
            date = "2026-05-29",
            routineItemId = 42L,
            isHidden = true,
        )

        assertEquals(
            HiddenChange(
                date = "2026-05-29",
                routineItemId = 42L,
                isHidden = true,
            ),
            repository.hiddenChanges.single(),
        )
    }

    @Test
    fun `given note when updating today item then repository receives the change`() = runTest {
        UpdateTodayItemNoteUseCase(repository)(
            date = "2026-05-29",
            routineItemId = 42L,
            note = "One liter was diet soda.",
        )

        assertEquals(
            NoteChange(
                date = "2026-05-29",
                routineItemId = 42L,
                note = "One liter was diet soda.",
            ),
            repository.noteChanges.single(),
        )
    }

    @Test
    fun `given completed count when updating today item then repository receives the change`() = runTest {
        UpdateTodayItemCompletedCountUseCase(repository)(
            date = "2026-05-29",
            routineItemId = 42L,
            completedCount = 3,
        )

        assertEquals(
            CountChange(
                date = "2026-05-29",
                routineItemId = 42L,
                completedCount = 3,
            ),
            repository.countChanges.single(),
        )
    }

    @Test
    fun `given summary note when updating today then repository receives the change`() = runTest {
        UpdateTodaySummaryNoteUseCase(repository)(
            date = "2026-05-29",
            note = "Low-energy day.",
        )

        assertEquals(
            SummaryNoteChange(
                date = "2026-05-29",
                note = "Low-energy day.",
            ),
            repository.summaryNoteChanges.single(),
        )
    }
}
