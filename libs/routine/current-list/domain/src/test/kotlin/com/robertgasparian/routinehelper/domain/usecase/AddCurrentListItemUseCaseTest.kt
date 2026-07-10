package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.AddedCurrentListItem
import com.robertgasparian.routinehelper.domain.repository.FakeCurrentListRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddCurrentListItemUseCaseTest {
    private val repository = FakeCurrentListRepository()
    private val useCase = AddCurrentListItemUseCase(repository)

    @Test
    fun `given valid input when adding item then trims title and optional description`() = runTest {
        val itemId = useCase(
            title = "  Pick up medicine  ",
            description = "  before 5 PM  ",
        )

        assertEquals(1L, itemId)
        assertEquals(
            listOf(
                AddedCurrentListItem(
                    title = "Pick up medicine",
                    description = "before 5 PM",
                ),
            ),
            repository.addedItems,
        )
    }

    @Test
    fun `given blank title when adding item then skips repository`() = runTest {
        val itemId = useCase(
            title = "   ",
            description = "description",
        )

        assertNull(itemId)
        assertEquals(emptyList<AddedCurrentListItem>(), repository.addedItems)
    }
}
