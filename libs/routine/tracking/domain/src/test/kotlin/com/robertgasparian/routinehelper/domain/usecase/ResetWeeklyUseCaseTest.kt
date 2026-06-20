package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResetWeeklyUseCaseTest {
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val useCase = ResetWeeklyUseCase(weeklyRepository)

    @Test
    fun `when resetting week then repository receives week start date`() = runTest {
        useCase("2026-05-24")

        assertEquals(listOf("2026-05-24"), weeklyRepository.resetWeeks)
    }
}
