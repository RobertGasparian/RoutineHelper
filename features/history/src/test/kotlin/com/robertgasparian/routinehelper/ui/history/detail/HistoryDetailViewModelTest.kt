package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateSnapshotSummaryNoteUseCase
import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.ui.share.ShareDraft
import com.robertgasparian.routinehelper.ui.history.FakeHistoryTextProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRoutineHistoryRepository()

    @Test
    fun `given weekly snapshot when observing state then snapshot details are mapped`() = runTest {
        val snapshotId = saveWeeklySnapshot()

        val state = createViewModel(snapshotId).uiState.first { !it.isLoading }

        assertEquals("2026-05-25", state.date)
        assertEquals(RoutineCadence.Weekly, state.cadence)
        assertEquals("12:00 PM", state.finalizedTime)
        assertEquals("Good week", state.summaryNote)
        assertEquals(
            listOf(
                HistoryDetailItemUiState(
                    actionId = 100L,
                    title = "Review goals",
                    description = "Review the weekly plan",
                    repeatTargetCount = 3,
                    completedCount = 2,
                    isChecked = false,
                    note = "Almost complete",
                ),
                HistoryDetailItemUiState(
                    actionId = 101L,
                    title = "Old task",
                    description = null,
                    repeatTargetCount = null,
                    completedCount = 0,
                    isChecked = false,
                    isHidden = true,
                    note = null,
                ),
            ),
            state.items,
        )
    }

    @Test
    fun `given missing snapshot when share is requested then missing state remains unchanged`() = runTest {
        val viewModel = createViewModel(snapshotId = 404L)
        val initialState = viewModel.uiState.first { !it.isLoading }

        viewModel.onIntent(HistoryDetailIntent.ShareClick)
        viewModel.onIntent(HistoryDetailIntent.ShareAsTextClick)
        viewModel.onIntent(HistoryDetailIntent.ShareAsFileClick)

        assertEquals(initialState, viewModel.uiState.first())
        assertTrue(initialState.isMissing)
    }

    @Test
    fun `given snapshot when text share is edited and dismissed then draft updates and clears`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onIntent(HistoryDetailIntent.ShareClick)
        assertTrue(viewModel.uiState.first { it.isShareFormatDialogVisible }.isShareFormatDialogVisible)

        viewModel.onIntent(HistoryDetailIntent.ShareAsTextClick)
        val initialDraft = requireNotNull(viewModel.uiState.first { it.shareDraft != null }.shareDraft)
        assertTrue(initialDraft is ShareDraft.Text)
        assertTrue(initialDraft.messageText.contains("Weekly routine snapshot"))

        viewModel.onIntent(HistoryDetailIntent.ShareTextChange("Updated message"))
        assertEquals(
            "Updated message",
            viewModel.uiState.first { state -> state.shareDraft?.messageText == "Updated message" }
                .shareDraft
                ?.messageText,
        )

        viewModel.onIntent(HistoryDetailIntent.ShareDismiss)
        val dismissedState = viewModel.uiState.first { state ->
            state.shareDraft == null && !state.isShareFormatDialogVisible
        }
        assertEquals(null, dismissedState.shareDraft)
        assertFalse(dismissedState.isShareFormatDialogVisible)
    }

    @Test
    fun `given weekly snapshot when file share is requested then weekly file draft is emitted`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onIntent(HistoryDetailIntent.ShareAsFileClick)
        val draft = requireNotNull(viewModel.uiState.first { it.shareDraft != null }.shareDraft) as ShareDraft.File

        assertEquals(
            "Here is the weekly routine snapshot from the week of 2026-05-25.",
            draft.messageText,
        )
        assertEquals("routine-snapshot-2026-05-25.txt", draft.fileName)
        assertTrue(draft.fileText.contains("Weekly routine snapshot"))
    }

    @Test
    fun `given editable snapshot when Reflection save is received then normalized note is persisted`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onIntent(
            HistoryDetailIntent.SaveSummaryNote("  Better week  "),
        )
        advanceUntilIdle()

        val savedState = viewModel.uiState.first { state -> state.summaryNote == "Better week" }
        assertEquals("Better week", savedState.summaryNote)
    }

    @Test
    fun `given editable snapshot when Reflection saves empty summary then note is removed`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onIntent(HistoryDetailIntent.SaveSummaryNote(""))
        advanceUntilIdle()

        val savedState = viewModel.uiState.first { state -> state.summaryNote.isEmpty() }
        assertEquals("", savedState.summaryNote)
    }

    @Test
    fun `given read only summary when Reflection save is received then save is ignored`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isLoading }

        val snapshot = requireNotNull(repository.snapshot(snapshotId).first())
        repository.setSnapshot(snapshot.copy(isSummaryNoteEditable = false))
        val readOnlyState = viewModel.uiState.first { state -> !state.isSummaryNoteEditable }

        viewModel.onIntent(HistoryDetailIntent.SaveSummaryNote("Should not save"))
        advanceUntilIdle()

        assertEquals("Good week", readOnlyState.summaryNote)
        assertEquals("Good week", repository.snapshot(snapshotId).first()?.summaryNote)
    }

    @Test
    fun `given snapshot when delete intent is handled then repository receives id and deleted event is emitted`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        val events = mutableListOf<HistoryDetailUiEvent>()
        val collectEventsJob = launch {
            viewModel.uiEvents.collect { event -> events += event }
        }

        viewModel.onIntent(HistoryDetailIntent.DeleteClick)
        advanceUntilIdle()

        assertEquals(listOf(snapshotId), repository.deletedSnapshotIds)
        assertEquals(listOf(HistoryDetailUiEvent.SnapshotDeleted), events)
        collectEventsJob.cancel()
    }

    private fun createViewModel(snapshotId: Long): HistoryDetailViewModel =
        HistoryDetailViewModel(
            snapshotId = snapshotId,
            deleteSnapshotUseCase = DeleteSnapshotUseCase(repository),
            historyTextProvider = FakeHistoryTextProvider(),
            snapshotUseCase = SnapshotUseCase(repository),
            updateSnapshotSummaryNoteUseCase = UpdateSnapshotSummaryNoteUseCase(repository),
        )

    private suspend fun saveWeeklySnapshot(): Long =
        repository.saveSnapshot(
            periodStartDate = "2026-05-25",
            finalizedAtMillis = 1_748_534_400_000L,
            items = listOf(
                RoutineSnapshotItem(
                    actionId = 100L,
                    title = "Review goals",
                    description = "Review the weekly plan",
                    position = 0,
                    isChecked = false,
                    note = "Almost complete",
                    repeatTargetCount = 3,
                    completedCount = 2,
                ),
                RoutineSnapshotItem(
                    actionId = 101L,
                    title = "Old task",
                    description = null,
                    position = 1,
                    isChecked = false,
                    isHidden = true,
                    note = null,
                ),
            ),
            summaryNote = "Good week",
            cadence = RoutineCadence.Weekly,
        )
}
