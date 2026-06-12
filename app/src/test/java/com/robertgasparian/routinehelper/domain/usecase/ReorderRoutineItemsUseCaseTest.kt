package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReorderRoutineItemsUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()

    @Test
    fun dailyReorderForwardsRoutineItemIdsInOrder() = runTest {
        ReorderDailyRoutineItemsUseCase(repository)(listOf(3L, 1L, 2L))

        assertEquals(listOf(listOf(3L, 1L, 2L)), repository.reorderedTemplateItemIds)
    }

    @Test
    fun weeklyReorderForwardsRoutineItemIdsInOrder() = runTest {
        ReorderWeeklyRoutineItemsUseCase(repository)(listOf(7L, 9L, 8L))

        assertEquals(listOf(listOf(7L, 9L, 8L)), repository.reorderedTemplateItemIds)
    }
}
