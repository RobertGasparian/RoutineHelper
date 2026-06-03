package com.robertgasparian.routinehelper.ui.daily

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.runtime.Composable
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class DailyComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                DailyComponentPaparazziContent(uiState = DailyUiState.preview())
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                DailyComponentPaparazziContent(uiState = DailyUiState.previewEmpty())
            }
        }
    }
}

@Composable
private fun DailyComponentPaparazziContent(
    uiState: DailyUiState,
) {
    DailyComponent(
        uiState = uiState,
        onEvent = {},
    )
}
