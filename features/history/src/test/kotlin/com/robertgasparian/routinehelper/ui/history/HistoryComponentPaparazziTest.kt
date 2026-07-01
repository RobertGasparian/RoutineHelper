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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
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
                    onIntent = {},
                )
            }
        }
    }
}
