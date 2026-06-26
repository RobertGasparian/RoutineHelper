package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.formatter.SnapshotShareTextFormatter
import com.robertgasparian.routinehelper.domain.usecase.DeleteSnapshotUseCase
import com.robertgasparian.routinehelper.domain.usecase.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.usecase.SnapshotUseCase
import com.robertgasparian.routinehelper.test.FixedTimeProvider
import com.robertgasparian.routinehelper.test.MainDispatcherRule
import com.robertgasparian.routinehelper.ui.share.ShareMode
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
class HistoryDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRoutineHistoryRepository()

    @Test
    fun `given weekly snapshot when observing state then snapshot details are mapped`() = runTest {
        val snapshotId = saveWeeklySnapshot()

        val state = createViewModel(snapshotId).uiState.first { !it.isMissing }

        assertEquals("Week of 2026-05-25", state.date)
        assertEquals(RoutineCadence.Weekly, state.cadence)
        assertEquals("Finalized 12:00 PM", state.finalizedLabel)
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
        val initialState = viewModel.uiState.first()

        viewModel.showShareOptions()
        viewModel.showTextSharePreview()
        viewModel.showFileSharePreview()

        assertEquals(initialState, viewModel.uiState.first())
        assertTrue(initialState.isMissing)
    }

    @Test
    fun `given snapshot when text share is edited and dismissed then draft updates and clears`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isMissing }

        viewModel.showShareOptions()
        assertTrue(viewModel.uiState.first { it.isShareFormatDialogVisible }.isShareFormatDialogVisible)

        viewModel.showTextSharePreview()
        val initialDraft = requireNotNull(viewModel.uiState.first { it.shareDraft != null }.shareDraft)
        assertEquals(ShareMode.Text, initialDraft.mode)
        assertTrue(initialDraft.messageText.contains("Weekly routine snapshot"))

        viewModel.updateShareText("Updated message")
        assertEquals("Updated message", viewModel.uiState.first().shareDraft?.messageText)

        viewModel.dismissSharePreview()
        val dismissedState = viewModel.uiState.first()
        assertEquals(null, dismissedState.shareDraft)
        assertFalse(dismissedState.isShareFormatDialogVisible)
    }

    @Test
    fun `given weekly snapshot when file share is requested then weekly file draft is emitted`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        viewModel.uiState.first { !it.isMissing }

        viewModel.showFileSharePreview()
        val draft = requireNotNull(viewModel.uiState.first { it.shareDraft != null }.shareDraft)

        assertEquals(ShareMode.File, draft.mode)
        assertEquals(
            "Here is the weekly routine snapshot from Week of 2026-05-25.",
            draft.messageText,
        )
        assertEquals("routine-snapshot-Week of 2026-05-25.txt", draft.fileName)
        assertTrue(draft.fileText.orEmpty().contains("Weekly routine snapshot"))
    }

    @Test
    fun `given snapshot when deleted then repository receives id and callback runs`() = runTest {
        val snapshotId = saveWeeklySnapshot()
        val viewModel = createViewModel(snapshotId)
        var wasDeleted = false

        viewModel.deleteSnapshot(onDeleted = { wasDeleted = true })
        advanceUntilIdle()

        assertEquals(listOf(snapshotId), repository.deletedSnapshotIds)
        assertTrue(wasDeleted)
    }

    private fun createViewModel(snapshotId: Long): HistoryDetailViewModel =
        HistoryDetailViewModel(
            snapshotId = snapshotId,
            deleteSnapshotUseCase = DeleteSnapshotUseCase(repository),
            snapshotShareTextFormatter = SnapshotShareTextFormatter(FixedTimeProvider()),
            snapshotUseCase = SnapshotUseCase(repository),
            timeProvider = FixedTimeProvider(),
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
