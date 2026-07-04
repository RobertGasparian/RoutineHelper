package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.UpdatedTemplateItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = UpdateTemplateItemUseCase(repository)

    @Test
    fun `given existing action values when updating template item then values are normalized`() = runTest {
        useCase(
            actionId = 42L,
            title = "  Stretch  ",
            description = "  Ten minutes  ",
            repeatTargetCount = 5,
        )

        assertEquals(
            UpdatedTemplateItem(
                actionId = 42L,
                title = "Stretch",
                description = "Ten minutes",
                repeatTargetCount = 5,
            ),
            repository.updatedItems.single(),
        )
    }

    @Test
    fun `given blank description when updating template item then description is null`() = runTest {
        useCase(
            actionId = 42L,
            title = "Stretch",
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
    fun `given repeat target below two when updating template item then target is discarded`() = runTest {
        useCase(
            actionId = 42L,
            title = "Pushups",
            description = null,
            repeatTargetCount = 1,
        )

        assertEquals(
            null,
            repository.updatedItems.single().repeatTargetCount,
        )
    }

    @Test
    fun `given blank title when updating template item then item is ignored`() = runTest {
        useCase(
            actionId = 42L,
            title = "   ",
            description = "Ignored",
        )

        assertTrue(repository.updatedItems.isEmpty())
    }
}
