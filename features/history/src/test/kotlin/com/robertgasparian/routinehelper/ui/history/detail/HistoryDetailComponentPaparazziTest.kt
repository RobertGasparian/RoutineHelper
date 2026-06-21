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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
                )
            }
        }
    }
}
