package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.UpdatedCurrentListItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCurrentListItemUseCaseTest {
    private val repository = FakeCurrentListRepository()
    private val useCase = UpdateCurrentListItemUseCase(repository)

    @Test
    fun `given valid input when updating item then trims title and optional description`() = runTest {
        useCase(
            itemId = 10L,
            title = "  Pick up medicine  ",
            description = "  before 5 PM  ",
        )

        assertEquals(
            listOf(
                UpdatedCurrentListItem(
                    itemId = 10L,
                    title = "Pick up medicine",
                    description = "before 5 PM",
                ),
            ),
            repository.updatedItems,
        )
    }

    @Test
    fun `given blank title when updating item then skips repository`() = runTest {
        useCase(
            itemId = 10L,
            title = "   ",
            description = "description",
        )

        assertEquals(emptyList<UpdatedCurrentListItem>(), repository.updatedItems)
    }
}
