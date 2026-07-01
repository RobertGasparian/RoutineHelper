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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
                )
            }
        }
    }
}
