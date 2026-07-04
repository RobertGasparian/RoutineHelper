package com.robertgasparian.routinehelper.ui.actioneditor

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.repository.AddedTemplateItem
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.UpdatedTemplateItem
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.RemoveTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.TemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTemplateItemUseCase
import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActionEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRoutineTemplateRepository()

    @Test
    fun `given no action id when observing state then empty create state is emitted`() = runTest {
        val state = createViewModel(actionId = null).uiState.first()

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

        val state = createViewModel(actionId = ACTION_ID).uiState.first { it.title == "Stretch" }

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
        val viewModel = createViewModel(actionId = null)

        viewModel.onIntent(ActionEditorIntent.TitleChange("Read"))
        viewModel.onIntent(ActionEditorIntent.DescriptionChange("One chapter"))
        viewModel.onIntent(ActionEditorIntent.RepeatEnabledChange(true))
        viewModel.onIntent(ActionEditorIntent.RepeatTargetCountChange(1))

        val state = viewModel.uiState.first { it.title == "Read" }

        assertEquals("Read", state.title)
        assertEquals("One chapter", state.description)
        assertTrue(state.isRepeatEnabled)
        assertEquals(2, state.repeatTargetCount)
    }

    @Test
    fun `given new weekly draft when save intent is handled then item is added and saved event is emitted`() = runTest {
        val viewModel = createViewModel(
            actionId = null,
            cadence = RoutineCadence.Weekly,
        )

        viewModel.onIntent(ActionEditorIntent.TitleChange("Review budget"))
        viewModel.onIntent(ActionEditorIntent.DescriptionChange("Check every category"))
        viewModel.onIntent(ActionEditorIntent.RepeatEnabledChange(true))
        viewModel.onIntent(ActionEditorIntent.RepeatTargetCountChange(4))
        val events = mutableListOf<ActionEditorUiEvent>()
        val collectEventsJob = launch {
            viewModel.uiEvents.collect { event -> events += event }
        }

        viewModel.onIntent(ActionEditorIntent.SaveClick)
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
        assertEquals(listOf(ActionEditorUiEvent.Saved), events)
        collectEventsJob.cancel()
    }

    @Test
    fun `given existing action when save intent is handled then item is updated and saved event is emitted`() = runTest {
        val viewModel = createViewModel(actionId = ACTION_ID)

        viewModel.onIntent(ActionEditorIntent.TitleChange("Updated title"))
        viewModel.onIntent(ActionEditorIntent.DescriptionChange("Updated description"))
        val events = mutableListOf<ActionEditorUiEvent>()
        val collectEventsJob = launch {
            viewModel.uiEvents.collect { event -> events += event }
        }

        viewModel.onIntent(ActionEditorIntent.SaveClick)
        advanceUntilIdle()

        assertEquals(
            UpdatedTemplateItem(
                actionId = ACTION_ID,
                title = "Updated title",
                description = "Updated description",
            ),
            repository.updatedItems.single(),
        )
        assertEquals(listOf(ActionEditorUiEvent.Saved), events)
        collectEventsJob.cancel()
    }

    @Test
    fun `given existing action when delete intent is handled then item is removed and deleted event is emitted`() = runTest {
        repository.setItems(listOf(templateItem()))
        val viewModel = createViewModel(actionId = ACTION_ID)
        val events = mutableListOf<ActionEditorUiEvent>()
        val collectEventsJob = launch {
            viewModel.uiEvents.collect { event -> events += event }
        }

        viewModel.onIntent(ActionEditorIntent.DeleteClick)
        advanceUntilIdle()

        assertEquals(listOf(ROUTINE_ITEM_ID), repository.removedTemplateItemIds)
        assertEquals(listOf(ActionEditorUiEvent.Deleted), events)
        collectEventsJob.cancel()
    }

    @Test
    fun `given no action id when delete intent is handled then repository is not invoked and no event is emitted`() = runTest {
        val viewModel = createViewModel(actionId = null)
        val events = mutableListOf<ActionEditorUiEvent>()
        val collectEventsJob = launch {
            viewModel.uiEvents.collect { event -> events += event }
        }

        viewModel.onIntent(ActionEditorIntent.DeleteClick)
        advanceUntilIdle()

        assertTrue(repository.removedTemplateItemIds.isEmpty())
        assertTrue(events.isEmpty())
        collectEventsJob.cancel()
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

    private fun createViewModel(
        actionId: Long?,
        cadence: RoutineCadence = RoutineCadence.Daily,
    ): ActionEditorViewModel =
        ActionEditorViewModel(
            actionId = actionId,
            cadence = cadence,
            addTemplateItemUseCase = AddTemplateItemUseCase(repository),
            removeTemplateItemUseCase = RemoveTemplateItemUseCase(repository),
            templateItemUseCase = TemplateItemUseCase(repository),
            updateTemplateItemUseCase = UpdateTemplateItemUseCase(repository),
        )

    private companion object {
        const val ACTION_ID = 42L
        const val ROUTINE_ITEM_ID = 7L
    }
}
