package com.robertgasparian.routinehelper.ui.history.detail

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.share.ShareDraft

data class HistoryDetailUiState(
    val date: String = "",
    val cadence: RoutineCadence = RoutineCadence.Daily,
    val finalizedLabel: String = "",
    val summaryNote: String = "",
    val items: List<HistoryDetailItemUiState> = emptyList(),
    val isMissing: Boolean = false,
    val isShareFormatDialogVisible: Boolean = false,
    val shareDraft: ShareDraft? = null,
) {
    val visibleItems: List<HistoryDetailItemUiState> = items.filterNot { item -> item.isHidden }
    val hiddenItems: List<HistoryDetailItemUiState> = items.filter { item -> item.isHidden }

    companion object {
        fun preview(): HistoryDetailUiState =
            HistoryDetailUiState(
                date = "2026-05-29",
                cadence = RoutineCadence.Daily,
                finalizedLabel = "Finalized 11:45 PM",
                summaryNote = "Low-energy day, but I kept the basics moving.",
                items = listOf(
                    HistoryDetailItemUiState(
                        actionId = 100,
                        title = "Drink water",
                        description = "Drink 3L water",
                        repeatTargetCount = null,
                        completedCount = 0,
                        isChecked = true,
                        note = "One liter was diet soda.",
                    ),
                    HistoryDetailItemUiState(
                        actionId = 101,
                        title = "Stretch",
                        description = "Ten minutes of mobility work",
                        repeatTargetCount = 3,
                        completedCount = 1,
                        isChecked = false,
                        isHidden = true,
                        note = null,
                    ),
                ),
            )

        fun previewEmpty(): HistoryDetailUiState =
            HistoryDetailUiState(
                date = "2026-05-29",
                finalizedLabel = "Finalized 11:45 PM",
            )

        fun previewShare(): HistoryDetailUiState =
            preview().copy(
                shareDraft = ShareDraft.text(
                    """
                    Routine snapshot
                    Date: 2026-05-29
                    Finalized: 11:45 PM

                    Summary note:
                    Low-energy day, but I kept the basics moving.

                    1. [x] Drink water
                       Description: Drink 3L water
                       Note: One liter was diet soda.

                    2. [ ] Stretch
                       Description: Ten minutes of mobility work
                    """.trimIndent(),
                ),
            )

        fun previewFileShare(): HistoryDetailUiState =
            preview().copy(
                shareDraft = ShareDraft.file(
                    messageText = "Here is the routine snapshot from 2026-05-29.",
                    fileText = previewShare().shareDraft?.messageText.orEmpty(),
                    fileName = "routine-snapshot-2026-05-29.txt",
                ),
            )

        fun previewShareOptions(): HistoryDetailUiState =
            preview().copy(
                isShareFormatDialogVisible = true,
            )

        fun previewMissing(): HistoryDetailUiState =
            HistoryDetailUiState(isMissing = true)
    }
}
