package com.robertgasparian.routinehelper.ui.currentlist

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class CurrentListComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given populated state when rendered then displays current list`() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                CurrentListComponent(
                    uiState = CurrentListUiState.preview(),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun `given empty state when rendered then displays empty current list`() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                CurrentListComponent(
                    uiState = CurrentListUiState.previewEmpty(),
                    onIntent = {},
                )
            }
        }
    }
}
