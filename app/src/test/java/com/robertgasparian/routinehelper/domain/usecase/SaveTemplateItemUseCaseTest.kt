package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = SaveTemplateItemUseCase(repository)

    @Test
    fun createsNewItemWhenActionIdIsNull() = runTest {
        useCase(
            actionId = null,
            title = "  Drink water  ",
            description = "  Drink 3L water  ",
        )

        assertEquals(
            AddedTemplateItem(
                title = "Drink water",
                description = "Drink 3L water",
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun updatesExistingItemWhenActionIdIsPresent() = runTest {
        useCase(
            actionId = 42L,
            title = "  Stretch  ",
            description = "   ",
        )

        assertEquals(
            UpdatedTemplateItem(
                actionId = 42L,
                title = "Stretch",
                description = null,
            ),
            repository.updatedItems.single(),
        )
    }

    @Test
    fun ignoresBlankTitle() = runTest {
        useCase(
            actionId = null,
            title = "   ",
            description = "Ignored",
        )

        assertTrue(repository.addedItems.isEmpty())
        assertTrue(repository.updatedItems.isEmpty())
    }
}
