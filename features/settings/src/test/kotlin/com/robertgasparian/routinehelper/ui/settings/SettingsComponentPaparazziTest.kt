package com.robertgasparian.routinehelper.ui.settings

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class SettingsComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given default settings when rendered then language and notification sections are shown`() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                SettingsComponent(
                    uiState = SettingsUiState.preview(),
                    onIntent = {},
                )
            }
        }
    }
}
