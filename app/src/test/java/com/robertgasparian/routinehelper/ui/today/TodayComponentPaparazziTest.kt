package com.robertgasparian.routinehelper.ui.today

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.runtime.Composable
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class TodayComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                TodayComponentPaparazziContent(uiState = TodayUiState.preview())
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                TodayComponentPaparazziContent(uiState = TodayUiState.previewEmpty())
            }
        }
    }
}

@Composable
private fun TodayComponentPaparazziContent(
    uiState: TodayUiState,
) {
    TodayComponent(
        uiState = uiState,
        onEvent = {},
    )
}
