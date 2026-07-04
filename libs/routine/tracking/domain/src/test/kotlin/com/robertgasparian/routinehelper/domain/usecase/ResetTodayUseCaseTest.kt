package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.FakeTodayRoutineRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResetTodayUseCaseTest {
    private val todayRepository = FakeTodayRoutineRepository()
    private val useCase = ResetTodayUseCase(todayRepository)

    @Test
    fun `when resetting today then repository receives date`() = runTest {
        useCase("2026-05-29")

        assertEquals(listOf("2026-05-29"), todayRepository.resetDates)
    }
}
