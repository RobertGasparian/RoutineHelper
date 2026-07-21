package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.share.ShareDraft

data class HistoryUiState(
    val snapshots: List<HistorySnapshotUiState> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.All,
    val isSelectionMode: Boolean = false,
    val selectedCount: Int = 0,
    val isShareFormatDialogVisible: Boolean = false,
    val shareDraft: ShareDraft? = null,
) {
    companion object {
        fun preview(): HistoryUiState =
            HistoryUiState(
                snapshots = listOf(
                    HistorySnapshotUiState(
                        snapshotId = 1,
                        date = "2026-05-29",
                        cadence = RoutineCadence.Daily,
                        completedCount = 2,
                        totalCount = 3,
                        hasSummaryNote = true,
                    ),
                    HistorySnapshotUiState(
                        snapshotId = 2,
                        date = "2026-05-24",
                        cadence = RoutineCadence.Weekly,
                        completedCount = 4,
                        totalCount = 5,
                    ),
                ),
            )

        fun previewSelection(): HistoryUiState =
            HistoryUiState(
                isSelectionMode = true,
                selectedCount = 1,
                snapshots = listOf(
                    HistorySnapshotUiState(
                        snapshotId = 1,
                        date = "2026-05-29",
                        cadence = RoutineCadence.Daily,
                        completedCount = 2,
                        totalCount = 3,
                        hasSummaryNote = true,
                        isSelected = true,
                    ),
                    HistorySnapshotUiState(
                        snapshotId = 2,
                        date = "2026-05-24",
                        cadence = RoutineCadence.Weekly,
                        completedCount = 4,
                        totalCount = 5,
                    ),
                ),
            )

        fun previewShare(): HistoryUiState =
            previewSelection().copy(
                shareDraft = ShareDraft.text(
                    """
                    Routine snapshot
                    Date: 2026-05-29
                    Finalized: 11:45 PM

                    1. [x] Drink water
                       Note: One liter was diet soda.
                    """.trimIndent(),
                ),
            )

        fun previewLongShare(): HistoryUiState =
            previewSelection().copy(
                shareDraft = ShareDraft.text(
                    buildString {
                        repeat(260) { index ->
                            appendLine("Routine snapshot")
                            appendLine("Date: 2026-05-${(index % 28) + 1}")
                            appendLine("Finalized: 11:45 PM")
                            appendLine()
                            appendLine("1. [x] Drink water")
                            appendLine("   Description: Drink 3L water")
                            appendLine("   Note: Export preview item $index")
                            appendLine()
                        }
                    }.trimEnd(),
                ),
            )

        fun previewFileShare(): HistoryUiState =
            previewSelection().copy(
                shareDraft = ShareDraft.file(
                    messageText = "Here are the routine snapshots from 2026-05-28 to 2026-05-29.",
                    fileText = previewShare().shareDraft?.messageText.orEmpty(),
                    fileName = "routine-snapshots-export.txt",
                ),
            )

        fun previewShareOptions(): HistoryUiState =
            previewSelection().copy(
                isShareFormatDialogVisible = true,
            )

        fun previewEmpty(): HistoryUiState = HistoryUiState()
    }
}
