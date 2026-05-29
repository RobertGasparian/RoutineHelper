package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = AddTemplateItemUseCase(repository)

    @Test
    fun trimsTitleAndDescriptionBeforeAddingItem() = runTest {
        val id = useCase(
            title = "  Drink water  ",
            description = "  Drink 3L water  ",
        )

        assertEquals(1L, id)
        assertEquals(
            AddedTemplateItem(
                title = "Drink water",
                description = "Drink 3L water",
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun convertsBlankDescriptionToNull() = runTest {
        useCase(
            title = "Stretch",
            description = "   ",
        )

        assertEquals(
            AddedTemplateItem(
                title = "Stretch",
                description = null,
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun ignoresBlankTitle() = runTest {
        val id = useCase(
            title = "   ",
            description = "Ignored",
        )

        assertEquals(0L, id)
        assertTrue(repository.addedItems.isEmpty())
    }
}
