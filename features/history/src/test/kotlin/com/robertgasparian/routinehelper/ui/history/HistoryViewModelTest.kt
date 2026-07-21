package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.ui.share.ShareDraft
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
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRoutineHistoryRepository()

    @Test
    fun `given snapshot summaries when observing state then history items are mapped`() = runTest {
        repository.setSnapshot(
            summary(
                snapshotId = DAILY_SNAPSHOT_ID,
                date = "2026-05-29",
                cadence = RoutineCadence.Daily,
                completedCount = 2,
                totalCount = 3,
                hasSummaryNote = true,
            ),
        )
        repository.setSnapshot(
            summary(
                snapshotId = WEEKLY_SNAPSHOT_ID,
                date = "2026-05-25",
                cadence = RoutineCadence.Weekly,
                completedCount = 4,
                totalCount = 4,
            ),
        )

        val state = createViewModel().uiState.first { it.snapshots.size == 2 }

        assertEquals(
            listOf(
                HistorySnapshotUiState(
                    snapshotId = DAILY_SNAPSHOT_ID,
                    date = "2026-05-29",
                    cadence = RoutineCadence.Daily,
                    completedCount = 2,
                    totalCount = 3,
                    hasSummaryNote = true,
                ),
                HistorySnapshotUiState(
                    snapshotId = WEEKLY_SNAPSHOT_ID,
                    date = "2026-05-25",
                    cadence = RoutineCadence.Weekly,
                    completedCount = 4,
                    totalCount = 4,
                    hasSummaryNote = false,
                ),
            ),
            state.snapshots,
        )
        assertEquals(HistoryFilter.All, state.selectedFilter)
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `given selected daily snapshot when weekly filter is selected then selection clears and weekly snapshot remains`() = runTest {
        repository.setSnapshot(summary(snapshotId = DAILY_SNAPSHOT_ID, cadence = RoutineCadence.Daily))
        repository.setSnapshot(summary(snapshotId = WEEKLY_SNAPSHOT_ID, cadence = RoutineCadence.Weekly))
        val viewModel = createViewModel()
        viewModel.uiState.first { it.snapshots.size == 2 }

        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(DAILY_SNAPSHOT_ID))
        assertEquals(1, viewModel.uiState.first { it.selectedCount == 1 }.selectedCount)

        viewModel.onIntent(HistoryIntent.FilterClick(HistoryFilter.Weekly))
        val state = viewModel.uiState.first { it.selectedFilter == HistoryFilter.Weekly }

        assertEquals(listOf(WEEKLY_SNAPSHOT_ID), state.snapshots.map(HistorySnapshotUiState::snapshotId))
        assertFalse(state.isSelectionMode)
        assertEquals(0, state.selectedCount)
    }

    @Test
    fun `given selected snapshot when share options are shown and cleared then dialog and selection reset`() = runTest {
        repository.setSnapshot(summary(snapshotId = DAILY_SNAPSHOT_ID, cadence = RoutineCadence.Daily))
        val viewModel = createViewModel()
        viewModel.uiState.first { it.snapshots.isNotEmpty() }

        viewModel.onIntent(HistoryIntent.ShareSelectedClick)
        assertFalse(viewModel.uiState.value.isShareFormatDialogVisible)

        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(DAILY_SNAPSHOT_ID))
        viewModel.onIntent(HistoryIntent.ShareSelectedClick)
        assertTrue(viewModel.uiState.first { it.isShareFormatDialogVisible }.isShareFormatDialogVisible)

        viewModel.onIntent(HistoryIntent.ClearSelectionClick)
        val state = viewModel.uiState.first { !it.isShareFormatDialogVisible }
        assertFalse(state.isSelectionMode)
        assertEquals(0, state.selectedCount)
    }

    @Test
    fun `given selected snapshots when text share is requested then combined text draft is emitted`() = runTest {
        val dailyId = saveSnapshot(date = "2026-05-29", cadence = RoutineCadence.Daily)
        val weeklyId = saveSnapshot(date = "2026-05-25", cadence = RoutineCadence.Weekly)
        val viewModel = createViewModel()
        viewModel.uiState.first { it.snapshots.size == 2 }
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(dailyId))
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(weeklyId))

        viewModel.onIntent(HistoryIntent.ShareAsTextClick)
        advanceUntilIdle()
        val draft = requireNotNull(viewModel.uiState.value.shareDraft)

        assertTrue(draft is ShareDraft.Text)
        assertTrue(draft.messageText.contains("Daily routine snapshot"))
        assertTrue(draft.messageText.contains("Weekly routine snapshot"))
    }

    @Test
    fun `given selected snapshots when file share is edited and dismissed then draft updates and clears`() = runTest {
        val firstId = saveSnapshot(date = "2026-05-25", cadence = RoutineCadence.Weekly)
        val secondId = saveSnapshot(date = "2026-05-29", cadence = RoutineCadence.Daily)
        val viewModel = createViewModel()
        viewModel.uiState.first { it.snapshots.size == 2 }
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(firstId))
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(secondId))

        viewModel.onIntent(HistoryIntent.ShareAsFileClick)
        advanceUntilIdle()
        val initialDraft = requireNotNull(viewModel.uiState.value.shareDraft) as ShareDraft.File

        assertEquals(
            "Here are the routine snapshots from 2026-05-25 to 2026-05-29.",
            initialDraft.messageText,
        )
        assertEquals("routine-snapshots-export.txt", initialDraft.fileName)

        viewModel.onIntent(HistoryIntent.ShareTextChange("Updated message"))
        viewModel.onIntent(HistoryIntent.ShareFileNameChange("updated.txt"))
        val updatedDraft = requireNotNull(
            viewModel.uiState.first { state ->
                (state.shareDraft as? ShareDraft.File)?.let { draft ->
                    draft.messageText == "Updated message" && draft.fileName == "updated.txt"
                } == true
            }.shareDraft,
        ) as ShareDraft.File
        assertEquals("Updated message", updatedDraft.messageText)
        assertEquals("updated.txt", updatedDraft.fileName)

        viewModel.onIntent(HistoryIntent.ShareDismiss)
        assertEquals(null, viewModel.uiState.first { it.shareDraft == null }.shareDraft)
    }

    @Test
    fun `given selected snapshots when deleted then repository receives ids and selection clears`() = runTest {
        val firstId = saveSnapshot(date = "2026-05-28", cadence = RoutineCadence.Daily)
        val secondId = saveSnapshot(date = "2026-05-29", cadence = RoutineCadence.Daily)
        val viewModel = createViewModel()
        viewModel.uiState.first { it.snapshots.size == 2 }
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(firstId))
        viewModel.onIntent(HistoryIntent.ToggleSnapshotSelection(secondId))

        viewModel.onIntent(HistoryIntent.DeleteSelectedClick)
        advanceUntilIdle()

        assertEquals(listOf(firstId, secondId), repository.deletedSnapshotIds)
        val state = viewModel.uiState.first { it.snapshots.isEmpty() }
        assertFalse(state.isSelectionMode)
        assertEquals(0, state.selectedCount)
    }

    private fun createViewModel(): HistoryViewModel =
        HistoryViewModel(
            deleteSnapshotUseCase = DeleteSnapshotUseCase(repository),
            historyTextProvider = FakeHistoryTextProvider(),
            snapshotSummariesUseCase = SnapshotSummariesUseCase(repository),
            snapshotUseCase = SnapshotUseCase(repository),
        )

    private suspend fun saveSnapshot(
        date: String,
        cadence: RoutineCadence,
    ): Long =
        repository.saveSnapshot(
            periodStartDate = date,
            finalizedAtMillis = 1_748_534_400_000L,
            items = listOf(snapshotItem()),
            summaryNote = "Summary",
            cadence = cadence,
        )

    private fun summary(
        snapshotId: Long,
        date: String = "2026-05-29",
        cadence: RoutineCadence,
        completedCount: Int = 0,
        totalCount: Int = 0,
        hasSummaryNote: Boolean = false,
    ): RoutineSnapshotSummary =
        RoutineSnapshotSummary(
            snapshotId = snapshotId,
            periodStartDate = date,
            finalizedAtMillis = 1_748_534_400_000L,
            cadence = cadence,
            completedCount = completedCount,
            totalCount = totalCount,
            hasSummaryNote = hasSummaryNote,
        )

    private fun snapshotItem(): RoutineSnapshotItem =
        RoutineSnapshotItem(
            actionId = 100L,
            title = "Drink water",
            description = "Drink 3L",
            position = 0,
            isChecked = true,
            note = "Done",
        )

    private companion object {
        const val DAILY_SNAPSHOT_ID = 1L
        const val WEEKLY_SNAPSHOT_ID = 2L
    }
}
