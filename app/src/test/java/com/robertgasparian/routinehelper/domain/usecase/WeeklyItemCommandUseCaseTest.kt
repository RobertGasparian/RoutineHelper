package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyItemCommandUseCaseTest {
    private val repository = FakeWeeklyRoutineRepository()

    @Test
    fun setCheckedForwardsWeekItemAndCheckedState() = runTest {
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
    fun updateNoteForwardsWeekItemAndNote() = runTest {
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
}
