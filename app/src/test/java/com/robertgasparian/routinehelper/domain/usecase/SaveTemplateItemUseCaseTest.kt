package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
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
                cadence = RoutineCadence.Daily,
            ),
            repository.addedItems.single(),
        )
    }

    @Test
    fun createsWeeklyItemWhenCadenceIsWeekly() = runTest {
        useCase(
            actionId = null,
            title = "Review budget",
            description = null,
            cadence = RoutineCadence.Weekly,
        )

        assertEquals(
            AddedTemplateItem(
                title = "Review budget",
                description = null,
                cadence = RoutineCadence.Weekly,
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
    fun savesRepeatTargetCountWhenGreaterThanOne() = runTest {
        useCase(
            actionId = null,
            title = "Pushups",
            description = null,
            repeatTargetCount = 5,
        )

        assertEquals(
            5,
            repository.addedItems.single().repeatTargetCount,
        )
    }

    @Test
    fun ignoresRepeatTargetCountWhenLessThanTwo() = runTest {
        useCase(
            actionId = null,
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
