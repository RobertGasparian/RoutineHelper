package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.FakeRoutineTemplateRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = RemoveTemplateItemUseCase(repository)

    @Test
    fun `when removing template item then repository receives routine item id`() = runTest {
        useCase(42L)

        assertEquals(listOf(42L), repository.removedTemplateItemIds)
    }
}
