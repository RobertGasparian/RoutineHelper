package com.robertgasparian.routinehelper.ui.history.detail

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class HistoryDetailComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.preview(),
                    onBackClick = {},
                    onShareClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                    onDeleteClick = {},
                )
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewEmpty(),
                    onBackClick = {},
                    onShareClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                    onDeleteClick = {},
                )
            }
        }
    }

    @Test
    fun shareDialog() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewShare(),
                    onBackClick = {},
                    onShareClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                    onDeleteClick = {},
                )
            }
        }
    }

    @Test
    fun shareOptions() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewShareOptions(),
                    onBackClick = {},
                    onShareClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                    onDeleteClick = {},
                )
            }
        }
    }

    @Test
    fun fileShareDialog() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewFileShare(),
                    onBackClick = {},
                    onShareClick = {},
                    onShareAsTextClick = {},
                    onShareAsFileClick = {},
                    onShareTextChange = {},
                    onShareDismiss = {},
                    onShareTextConfirm = {},
                    onShareFileConfirm = {},
                    onDeleteClick = {},
                )
            }
        }
    }
}
