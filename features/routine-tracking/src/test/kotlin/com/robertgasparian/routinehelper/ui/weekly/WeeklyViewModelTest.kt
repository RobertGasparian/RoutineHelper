package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.removal.FakeRoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalRequest
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.repository.AddedTemplateItem
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.WeeklyCheckedChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyCountChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyHiddenChange
import com.robertgasparian.routinehelper.domain.repository.WeeklyNoteChange
import com.robertgasparian.routinehelper.domain.repository.WeeklySummaryNoteChange
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderWeeklyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.test.FakeNoteDateTimeTextProvider
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingDebugItemsPopulator
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent
import com.robertgasparian.routinehelper.ui.tracking.englishDebugTextProvider
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val templateRepository = FakeRoutineTemplateRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val noteDateTimeTextProvider = FakeNoteDateTimeTextProvider()
    private val timeProvider = FixedTimeProvider()
    private val removalUndoCoordinator = FakeRoutineRemovalUndoCoordinator()

    @Test
    fun `when item events are received then forwards them to weekly use cases`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(RoutineTrackingIntent.CheckedChange(routineItemId = 10L, isChecked = true))
        viewModel.onIntent(RoutineTrackingIntent.CompletedCountChange(routineItemId = 10L, completedCount = 3))
        viewModel.onIntent(RoutineTrackingIntent.HiddenChange(routineItemId = 10L, isHidden = true))
        viewModel.onIntent(RoutineTrackingIntent.RemoveItem(routineItemId = 10L))
        viewModel.onIntent(RoutineTrackingIntent.ReorderItems(listOf(10L, 11L)))
        advanceUntilIdle()

        assertEquals(
            listOf(
                WeeklyCheckedChange(
                    weekStartDate = WeekStartDate,
                    routineItemId = 10L,
                    isChecked = true,
                ),
            ),
            weeklyRepository.checkedChanges,
        )
        assertEquals(
            listOf(
                WeeklyCountChange(
                    weekStartDate = WeekStartDate,
                    routineItemId = 10L,
                    completedCount = 3,
                ),
            ),
            weeklyRepository.countChanges,
        )
        assertEquals(
            listOf(
                WeeklyHiddenChange(
                    weekStartDate = WeekStartDate,
                    routineItemId = 10L,
                    isHidden = true,
                ),
            ),
            weeklyRepository.hiddenChanges,
        )
        assertEquals(
            listOf(RoutineRemovalRequest(RoutineRemovalSource.Weekly, itemId = 10L)),
            removalUndoCoordinator.removalRequests,
        )
        assertEquals(listOf(listOf(10L, 11L)), templateRepository.reorderedTemplateItemIds)
    }

    @Test
    fun `given item note editor is open when save is clicked then updates weekly item note`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(editNoteClick(note = "old note"))
        viewModel.onIntent(noteDraftChange("updated note"))
        viewModel.onIntent(RoutineTrackingIntent.NoteEditorSaveClick)
        advanceUntilIdle()

        assertEquals(
            listOf(
                WeeklyNoteChange(
                    weekStartDate = WeekStartDate,
                    routineItemId = 10L,
                    note = "updated note",
                ),
            ),
            weeklyRepository.noteChanges,
        )
    }

    @Test
    fun `when Reflection summary save is received then forwards it to weekly summary use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(RoutineTrackingIntent.SaveSummaryNote("  Better week  "))
        advanceUntilIdle()

        assertEquals(
            listOf(
                WeeklySummaryNoteChange(
                    weekStartDate = WeekStartDate,
                    note = "  Better week  ",
                ),
            ),
            weeklyRepository.summaryNoteChanges,
        )
    }

    @Test
    fun `given item note editor is open when date is inserted then saves provider date text at cursor`() = runTest {
        noteDateTimeTextProvider.dateText = "May 29"
        val viewModel = createViewModel()

        viewModel.onIntent(editNoteClick(note = "Completed on "))
        viewModel.onIntent(RoutineTrackingIntent.NoteDraftDateClick)
        viewModel.onIntent(RoutineTrackingIntent.NoteEditorSaveClick)
        advanceUntilIdle()

        assertEquals(
            "Completed on May 29",
            weeklyRepository.noteChanges.single().note,
        )
    }

    @Test
    fun `given weekly list has an item when test items are added then appends twenty weekly actions`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = WeekStartDate,
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 1L,
                    actionId = 1L,
                    title = "Existing weekly action",
                    description = null,
                    position = 0,
                    weekStartDate = WeekStartDate,
                    isChecked = false,
                    note = null,
                ),
            ),
        )
        val viewModel = createViewModel()
        viewModel.uiState.first { state -> state.items.size == 1 }

        viewModel.onIntent(RoutineTrackingIntent.AddTestItemsClick)
        advanceUntilIdle()

        assertEquals(20, templateRepository.addedItems.size)
        assertEquals(
            AddedTemplateItem(
                title = "weekly action 2",
                description = "description for weekly action 2",
                cadence = RoutineCadence.Weekly,
            ),
            templateRepository.addedItems.first(),
        )
        assertEquals(
            AddedTemplateItem(
                title = "weekly action 21",
                description = null,
                cadence = RoutineCadence.Weekly,
            ),
            templateRepository.addedItems.last(),
        )
    }

    @Test
    fun `given current week has items when snapshot week is selected then finalizes current week under selected week`() = runTest {
        weeklyRepository.setItems(
            weekStartDate = WeekStartDate,
            items = listOf(
                WeeklyRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Stretch",
                    description = null,
                    position = 0,
                    weekStartDate = WeekStartDate,
                    isChecked = true,
                    note = "Done.",
                ),
            ),
        )
        val viewModel = createViewModel()

        viewModel.onIntent(RoutineTrackingIntent.SnapshotDateSelected("2026-05-18"))
        advanceUntilIdle()

        assertEquals("2026-05-18", historyRepository.savedSnapshots.single().periodStartDate)
        assertEquals(
            Instant.parse("2026-05-29T14:30:00Z").toEpochMilli(),
            historyRepository.savedSnapshots.single().finalizedAtMillis,
        )
    }

    private fun createViewModel(): WeeklyViewModel =
        WeeklyViewModel(
            weeklyItemsUseCase = WeeklyItemsUseCase(weeklyRepository),
            weeklySummaryNoteUseCase = WeeklySummaryNoteUseCase(weeklyRepository),
            debugItemsPopulator = RoutineTrackingDebugItemsPopulator(
                addTemplateItemUseCase = AddTemplateItemUseCase(templateRepository),
                debugTextProvider = englishDebugTextProvider,
            ),
            finalizeWeeklyUseCase = FinalizeWeeklyUseCase(
                weeklyRoutineRepository = weeklyRepository,
                routineHistoryRepository = historyRepository,
            ),
            routineRemovalUndoCoordinator = removalUndoCoordinator,
            reorderWeeklyRoutineItemsUseCase = ReorderWeeklyRoutineItemsUseCase(templateRepository),
            setWeeklyItemCheckedUseCase = SetWeeklyItemCheckedUseCase(weeklyRepository),
            setWeeklyItemHiddenUseCase = SetWeeklyItemHiddenUseCase(weeklyRepository),
            updateWeeklyItemCompletedCountUseCase = UpdateWeeklyItemCompletedCountUseCase(weeklyRepository),
            updateWeeklyItemNoteUseCase = UpdateWeeklyItemNoteUseCase(weeklyRepository),
            updateWeeklySummaryNoteUseCase = UpdateWeeklySummaryNoteUseCase(weeklyRepository),
            noteDateTimeTextProvider = noteDateTimeTextProvider,
            timeProvider = timeProvider,
        )

    private fun editNoteClick(note: String): RoutineTrackingIntent.EditNoteClick =
        RoutineTrackingIntent.EditNoteClick(
            routineItemId = 10L,
            itemTitle = "Stretch",
            note = note,
        )

    private fun noteDraftChange(text: String): RoutineTrackingIntent.NoteDraftChange =
        RoutineTrackingIntent.NoteDraftChange(
            text = text,
            selectionStart = text.length,
            selectionEnd = text.length,
        )

    private companion object {
        const val WeekStartDate = "2026-05-25"
    }
}
