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
                HistoryComponent(uiState = HistoryUiState.preview())
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                HistoryComponent(uiState = HistoryUiState.previewEmpty())
            }
        }
    }
}
