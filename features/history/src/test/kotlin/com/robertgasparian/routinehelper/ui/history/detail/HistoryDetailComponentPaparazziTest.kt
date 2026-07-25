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
                    showDebugNotificationAction = false,
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
                    showDebugNotificationAction = false,
                )
            }
        }
    }

    @Test
    fun withoutSummaryNote() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewWithoutSummaryNote(),
                    onIntent = {},
                    showDebugNotificationAction = false,
                )
            }
        }
    }

    @Test
    fun readOnlySummaryNote() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryDetailComponent(
                    uiState = HistoryDetailUiState.previewReadOnlySummaryNote(),
                    onIntent = {},
                    showDebugNotificationAction = false,
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
                    showDebugNotificationAction = false,
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
                    showDebugNotificationAction = false,
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
                    showDebugNotificationAction = false,
                )
            }
        }
    }
}
