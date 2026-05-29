package com.robertgasparian.routinehelper.ui.history

data class HistoryUiState(
    val snapshots: List<HistorySnapshotUiState> = emptyList(),
) {
    companion object {
        fun preview(): HistoryUiState =
            HistoryUiState(
                snapshots = listOf(
                    HistorySnapshotUiState(
                        snapshotId = 1,
                        date = "2026-05-29",
                        finalizedLabel = "Finalized 11:45 PM",
                    ),
                    HistorySnapshotUiState(
                        snapshotId = 2,
                        date = "2026-05-28",
                        finalizedLabel = "Finalized 11:38 PM",
                    ),
                ),
            )

        fun previewEmpty(): HistoryUiState = HistoryUiState()
    }
}

data class HistorySnapshotUiState(
    val snapshotId: Long,
    val date: String,
    val finalizedLabel: String,
)
