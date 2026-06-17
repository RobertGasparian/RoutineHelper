package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderWeeklyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyCheckedChange
import com.robertgasparian.routinehelper.domain.usecase.WeeklyCountChange
import com.robertgasparian.routinehelper.domain.usecase.WeeklyHiddenChange
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyNoteChange
import com.robertgasparian.routinehelper.domain.usecase.WeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.test.FakeNoteDateTimeTextProvider
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import com.robertgasparian.routinehelper.test.MainDispatcherRule
import com.robertgasparian.routinehelper.ui.daily.DailyItemUiState
import com.robertgasparian.routinehelper.ui.daily.DailyUiEvent
import com.robertgasparian.routinehelper.ui.daily.NoteDraftUiState
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `when item events are received then forwards them to weekly use cases`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(DailyUiEvent.CheckedChange(routineItemId = 10L, isChecked = true))
        viewModel.onEvent(DailyUiEvent.CompletedCountChange(routineItemId = 10L, completedCount = 3))
        viewModel.onEvent(DailyUiEvent.HiddenChange(routineItemId = 10L, isHidden = true))
        viewModel.onEvent(DailyUiEvent.ReorderItems(listOf(10L, 11L)))
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
        assertEquals(listOf(listOf(10L, 11L)), templateRepository.reorderedTemplateItemIds)
    }

    @Test
    fun `given item note editor is open when save is clicked then updates weekly item note`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(DailyUiEvent.EditNoteClick(itemUiState(note = "old note")))
        viewModel.onEvent(DailyUiEvent.NoteDraftChange(NoteDraftUiState.fromText("updated note")))
        viewModel.onEvent(DailyUiEvent.NoteEditorSaveClick)
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
    fun `given item note editor is open when date is inserted then saves provider date text at cursor`() = runTest {
        noteDateTimeTextProvider.dateText = "May 29"
        val viewModel = createViewModel()

        viewModel.onEvent(DailyUiEvent.EditNoteClick(itemUiState(note = "Completed on ")))
        viewModel.onEvent(DailyUiEvent.NoteDraftDateClick)
        viewModel.onEvent(DailyUiEvent.NoteEditorSaveClick)
        advanceUntilIdle()

        assertEquals(
            "Completed on May 29",
            weeklyRepository.noteChanges.single().note,
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

        viewModel.onEvent(DailyUiEvent.SnapshotDateSelected("2026-05-18"))
        advanceUntilIdle()

        assertEquals("2026-05-18", historyRepository.savedSnapshots.single().date)
        assertEquals(
            Instant.parse("2026-05-29T14:30:00Z").toEpochMilli(),
            historyRepository.savedSnapshots.single().finalizedAtMillis,
        )
    }

    private fun createViewModel(): WeeklyViewModel =
        WeeklyViewModel(
            weeklyItemsUseCase = WeeklyItemsUseCase(weeklyRepository),
            weeklySummaryNoteUseCase = WeeklySummaryNoteUseCase(weeklyRepository),
            finalizeWeeklyUseCase = FinalizeWeeklyUseCase(
                weeklyRoutineRepository = weeklyRepository,
                routineHistoryRepository = historyRepository,
            ),
            reorderWeeklyRoutineItemsUseCase = ReorderWeeklyRoutineItemsUseCase(templateRepository),
            setWeeklyItemCheckedUseCase = SetWeeklyItemCheckedUseCase(weeklyRepository),
            setWeeklyItemHiddenUseCase = SetWeeklyItemHiddenUseCase(weeklyRepository),
            updateWeeklyItemCompletedCountUseCase = UpdateWeeklyItemCompletedCountUseCase(weeklyRepository),
            updateWeeklyItemNoteUseCase = UpdateWeeklyItemNoteUseCase(weeklyRepository),
            updateWeeklySummaryNoteUseCase = UpdateWeeklySummaryNoteUseCase(weeklyRepository),
            noteDateTimeTextProvider = noteDateTimeTextProvider,
            timeProvider = timeProvider,
        )

    private fun itemUiState(note: String): DailyItemUiState =
        DailyItemUiState(
            routineItemId = 10L,
            actionId = 100L,
            title = "Stretch",
            description = null,
            repeatTargetCount = null,
            completedCount = 0,
            isChecked = false,
            note = note,
        )

    private companion object {
        const val WeekStartDate = "2026-05-25"
    }
}
