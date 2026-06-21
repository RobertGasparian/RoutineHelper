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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
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
                    onEvent = {},
                )
            }
        }
    }
}
