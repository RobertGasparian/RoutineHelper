package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayItemCommandUseCaseTest {
    private val repository = FakeTodayRoutineRepository()

    @Test
    fun setCheckedForwardsDateItemAndCheckedState() = runTest {
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
    fun updateNoteForwardsDateItemAndNote() = runTest {
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
    fun updateCompletedCountForwardsDateItemAndCount() = runTest {
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
}
