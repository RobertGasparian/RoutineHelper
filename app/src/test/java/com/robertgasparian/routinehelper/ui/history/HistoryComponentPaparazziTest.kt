package com.robertgasparian.routinehelper.ui.history

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class HistoryComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.preview(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewEmpty(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun selection() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewSelection(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun shareDialog() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewShare(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun shareOptions() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewShareOptions(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun fileShareDialog() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewFileShare(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }

    @Test
    fun longShareDialog() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(
                    uiState = HistoryUiState.previewLongShare(),
                    onSnapshotClick = {},
                    onSnapshotLongClick = {},
                    onClearSelectionClick = {},
                    onShareSelectedClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onDeleteSelectedClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                )
            }
        }
    }
}
