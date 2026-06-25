package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTemplateItemUseCaseTest {
    private val repository = FakeRoutineTemplateRepository()
    private val useCase = AddTemplateItemUseCase(repository)

    @Test
    fun `given padded weekly values when adding template item then values are normalized and cadence is preserved`() = runTest {
        val id = useCase(
            title = "  Drink water  ",
            description = "  Drink 3L water  ",
            repeatTargetCount = 5,
            cadence = RoutineCadence.Weekly,
        )

        assertEquals(1L, id)
        assertEquals(
            AddedTemplateItem(
                title = "Drink water",
                description = "Drink 3L water",
                repeatTargetCount = 5,
                cadence = RoutineCadence.Weekly,
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun `given blank description when adding template item then description is null`() = runTest {
        useCase(
            title = "Stretch",
            description = "   ",
        )

        assertEquals(
            AddedTemplateItem(
                title = "Stretch",
                description = null,
                cadence = RoutineCadence.Daily,
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun `given repeat target below two when adding template item then target is discarded`() = runTest {
        useCase(
            title = "Pushups",
            description = null,
            repeatTargetCount = 1,
        )

        assertEquals(
            null,
            repository.addedItems.single().repeatTargetCount,
        )
    }

    @Test
    fun `given blank title when adding template item then item is ignored`() = runTest {
        val id = useCase(
            title = "   ",
            description = "Ignored",
        )

        assertEquals(null, id)
        assertTrue(repository.addedItems.isEmpty())
    }
}
