package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = RemoveTemplateItemUseCase(repository)

    @Test
    fun removesTemplateItemByRoutineItemId() = runTest {
        useCase(42L)

        assertEquals(listOf(42L), repository.removedTemplateItemIds)
    }
}
