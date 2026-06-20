package com.robertgasparian.routinehelper.ui.daily

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.CheckedChange
import com.robertgasparian.routinehelper.domain.usecase.CountChange
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.usecase.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.HiddenChange
import com.robertgasparian.routinehelper.domain.usecase.NoteChange
import com.robertgasparian.routinehelper.domain.usecase.ReorderDailyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodaySummaryNoteUseCase
import com.robertgasparian.routinehelper.test.FakeNoteDateTimeTextProvider
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import com.robertgasparian.routinehelper.test.MainDispatcherRule
import com.robertgasparian.routinehelper.ui.tracking.NoteDraftUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingItemUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiEvent
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val todayRepository = FakeTodayRoutineRepository()
    private val templateRepository = FakeRoutineTemplateRepository()
    private val historyRepository = FakeRoutineHistoryRepository()
    private val noteDateTimeTextProvider = FakeNoteDateTimeTextProvider()
    private val timeProvider = FixedTimeProvider()

    @Test
    fun `when item events are received then forwards them to daily use cases`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(RoutineTrackingUiEvent.CheckedChange(routineItemId = 10L, isChecked = true))
        viewModel.onEvent(RoutineTrackingUiEvent.CompletedCountChange(routineItemId = 10L, completedCount = 3))
        viewModel.onEvent(RoutineTrackingUiEvent.HiddenChange(routineItemId = 10L, isHidden = true))
        viewModel.onEvent(RoutineTrackingUiEvent.ReorderItems(listOf(10L, 11L)))
        advanceUntilIdle()

        assertEquals(
            listOf(
                CheckedChange(
                    date = TodayDate,
                    routineItemId = 10L,
                    isChecked = true,
                ),
            ),
            todayRepository.checkedChanges,
        )
        assertEquals(
            listOf(
                CountChange(
                    date = TodayDate,
                    routineItemId = 10L,
                    completedCount = 3,
                ),
            ),
            todayRepository.countChanges,
        )
        assertEquals(
            listOf(
                HiddenChange(
                    date = TodayDate,
                    routineItemId = 10L,
                    isHidden = true,
                ),
            ),
            todayRepository.hiddenChanges,
        )
        assertEquals(listOf(listOf(10L, 11L)), templateRepository.reorderedTemplateItemIds)
    }

    @Test
    fun `given item note editor is open when save is clicked then updates item note`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(RoutineTrackingUiEvent.EditNoteClick(itemUiState(note = "old note")))
        viewModel.onEvent(RoutineTrackingUiEvent.NoteDraftChange(NoteDraftUiState.fromText("updated note")))
        viewModel.onEvent(RoutineTrackingUiEvent.NoteEditorSaveClick)
        advanceUntilIdle()

        assertEquals(
            listOf(
                NoteChange(
                    date = TodayDate,
                    routineItemId = 10L,
                    note = "updated note",
                ),
            ),
            todayRepository.noteChanges,
        )
    }

    @Test
    fun `given item note editor is open when date is inserted then saves provider date text at cursor`() = runTest {
        noteDateTimeTextProvider.dateText = "May 29"
        val viewModel = createViewModel()

        viewModel.onEvent(RoutineTrackingUiEvent.EditNoteClick(itemUiState(note = "Completed on ")))
        viewModel.onEvent(RoutineTrackingUiEvent.NoteDraftDateClick)
        viewModel.onEvent(RoutineTrackingUiEvent.NoteEditorSaveClick)
        advanceUntilIdle()

        assertEquals(
            "Completed on May 29",
            todayRepository.noteChanges.single().note,
        )
    }

    @Test
    fun `given current day has items when snapshot date is selected then finalizes current day under selected date`() = runTest {
        todayRepository.setItems(
            date = TodayDate,
            items = listOf(
                TodayRoutineItem(
                    routineItemId = 10L,
                    actionId = 100L,
                    title = "Drink water",
                    description = null,
                    position = 0,
                    date = TodayDate,
                    isChecked = true,
                    note = "Done.",
                ),
            ),
        )
        val viewModel = createViewModel()

        viewModel.onEvent(RoutineTrackingUiEvent.SnapshotDateSelected("2026-05-28"))
        advanceUntilIdle()

        assertEquals("2026-05-28", historyRepository.savedSnapshots.single().date)
        assertEquals(
            Instant.parse("2026-05-29T14:30:00Z").toEpochMilli(),
            historyRepository.savedSnapshots.single().finalizedAtMillis,
        )
    }

    private fun createViewModel(): DailyViewModel =
        DailyViewModel(
            todayItemsUseCase = TodayItemsUseCase(todayRepository),
            todaySummaryNoteUseCase = TodaySummaryNoteUseCase(todayRepository),
            finalizeTodayUseCase = FinalizeTodayUseCase(
                todayRoutineRepository = todayRepository,
                routineHistoryRepository = historyRepository,
            ),
            reorderDailyRoutineItemsUseCase = ReorderDailyRoutineItemsUseCase(templateRepository),
            setTodayItemCheckedUseCase = SetTodayItemCheckedUseCase(todayRepository),
            setTodayItemHiddenUseCase = SetTodayItemHiddenUseCase(todayRepository),
            updateTodayItemCompletedCountUseCase = UpdateTodayItemCompletedCountUseCase(todayRepository),
            updateTodayItemNoteUseCase = UpdateTodayItemNoteUseCase(todayRepository),
            updateTodaySummaryNoteUseCase = UpdateTodaySummaryNoteUseCase(todayRepository),
            noteDateTimeTextProvider = noteDateTimeTextProvider,
            timeProvider = timeProvider,
        )

    private fun itemUiState(note: String): RoutineTrackingItemUiState =
        RoutineTrackingItemUiState(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            description = null,
            repeatTargetCount = null,
            completedCount = 0,
            isChecked = false,
            note = note,
        )

    private companion object {
        const val TodayDate = "2026-05-29"
    }
}
