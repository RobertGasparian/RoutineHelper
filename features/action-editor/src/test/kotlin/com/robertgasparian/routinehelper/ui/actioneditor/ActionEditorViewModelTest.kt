package com.robertgasparian.routinehelper.ui.actioneditor

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.AddedTemplateItem
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.usecase.RemoveTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.TemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdatedTemplateItem
import com.robertgasparian.routinehelper.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActionEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRoutineTemplateRepository()
    private val viewModel = ActionEditorViewModel(
        addTemplateItemUseCase = AddTemplateItemUseCase(repository),
        removeTemplateItemUseCase = RemoveTemplateItemUseCase(repository),
        templateItemUseCase = TemplateItemUseCase(repository),
        updateTemplateItemUseCase = UpdateTemplateItemUseCase(repository),
    )

    @Test
    fun `given no action id when observing state then empty create state is emitted`() = runTest {
        val state = viewModel.uiState(actionId = null).first()

        assertEquals(ActionEditorUiState.previewEmpty(), state)
    }

    @Test
    fun `given existing action when observing state then saved values populate the draft`() = runTest {
        repository.setItems(
            listOf(
                templateItem(
                    title = "Stretch",
                    description = "Ten minutes",
                    repeatTargetCount = 3,
                ),
            ),
        )

        val state = viewModel.uiState(ACTION_ID).first { it.title == "Stretch" }

        assertEquals(
            ActionEditorUiState(
                title = "Stretch",
                description = "Ten minutes",
                isRepeatEnabled = true,
                repeatTargetCount = 3,
                isEditing = true,
            ),
            state,
        )
    }

    @Test
    fun `given draft changes when observing state then updated values are emitted`() = runTest {
        viewModel.updateTitle("Read")
        viewModel.updateDescription("One chapter")
        viewModel.updateRepeatEnabled(true)
        viewModel.updateRepeatTargetCount(1)

        val state = viewModel.uiState(actionId = null).first()

        assertEquals("Read", state.title)
        assertEquals("One chapter", state.description)
        assertTrue(state.isRepeatEnabled)
        assertEquals(2, state.repeatTargetCount)
    }

    @Test
    fun `given new weekly draft when saving then item and callback are forwarded`() = runTest {
        viewModel.updateTitle("Review budget")
        viewModel.updateDescription("Check every category")
        viewModel.updateRepeatEnabled(true)
        viewModel.updateRepeatTargetCount(4)
        var wasSaved = false

        viewModel.save(
            actionId = null,
            cadence = RoutineCadence.Weekly,
            onSaved = { wasSaved = true },
        )
        advanceUntilIdle()

        assertEquals(
            AddedTemplateItem(
                title = "Review budget",
                description = "Check every category",
                repeatTargetCount = 4,
                cadence = RoutineCadence.Weekly,
            ),
            repository.addedItems.single(),
        )
        assertTrue(wasSaved)
    }

    @Test
    fun `given existing action when saving then update and callback are forwarded`() = runTest {
        viewModel.updateTitle("Updated title")
        viewModel.updateDescription("Updated description")
        var wasSaved = false

        viewModel.save(
            actionId = ACTION_ID,
            cadence = RoutineCadence.Daily,
            onSaved = { wasSaved = true },
        )
        advanceUntilIdle()

        assertEquals(
            UpdatedTemplateItem(
                actionId = ACTION_ID,
                title = "Updated title",
                description = "Updated description",
            ),
            repository.updatedItems.single(),
        )
        assertTrue(wasSaved)
    }

    @Test
    fun `given existing action when deleting then item and callback are forwarded`() = runTest {
        repository.setItems(listOf(templateItem()))
        var wasDeleted = false

        viewModel.delete(
            actionId = ACTION_ID,
            onDeleted = { wasDeleted = true },
        )
        advanceUntilIdle()

        assertEquals(listOf(ROUTINE_ITEM_ID), repository.removedTemplateItemIds)
        assertTrue(wasDeleted)
    }

    @Test
    fun `given no action id when deleting then repository and callback are not invoked`() = runTest {
        var wasDeleted = false

        viewModel.delete(
            actionId = null,
            onDeleted = { wasDeleted = true },
        )
        advanceUntilIdle()

        assertTrue(repository.removedTemplateItemIds.isEmpty())
        assertFalse(wasDeleted)
    }

    private fun templateItem(
        title: String = "Drink water",
        description: String? = "Drink 3L",
        repeatTargetCount: Int? = null,
    ): RoutineTemplateItem =
        RoutineTemplateItem(
            routineItemId = ROUTINE_ITEM_ID,
            actionId = ACTION_ID,
            title = title,
            description = description,
            position = 0,
            cadence = RoutineCadence.Daily,
            repeatTargetCount = repeatTargetCount,
        )

    private companion object {
        const val ACTION_ID = 42L
        const val ROUTINE_ITEM_ID = 7L
    }
}
