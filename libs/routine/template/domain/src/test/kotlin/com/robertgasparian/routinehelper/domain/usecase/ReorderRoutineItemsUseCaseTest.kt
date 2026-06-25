package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReorderRoutineItemsUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()

    @Test
    fun `given daily item ids when reordering then repository receives daily cadence and ids in order`() = runTest {
        ReorderDailyRoutineItemsUseCase(repository)(listOf(3L, 1L, 2L))

        assertEquals(listOf(RoutineCadence.Daily), repository.reorderedTemplateItemCadences)
        assertEquals(listOf(listOf(3L, 1L, 2L)), repository.reorderedTemplateItemIds)
    }

    @Test
    fun `given weekly item ids when reordering then repository receives weekly cadence and ids in order`() = runTest {
        ReorderWeeklyRoutineItemsUseCase(repository)(listOf(7L, 9L, 8L))

        assertEquals(listOf(RoutineCadence.Weekly), repository.reorderedTemplateItemCadences)
        assertEquals(listOf(listOf(7L, 9L, 8L)), repository.reorderedTemplateItemIds)
    }
}
