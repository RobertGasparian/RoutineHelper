package com.robertgasparian.routinehelper.ui.removalundo

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class RoutineRemovalUndoSnackbarHostPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given daily removals when rendered then displays source aware undo actions`() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                RoutineRemovalUndoSnackbarHost(
                    uiState = RoutineRemovalUndoUiState.preview(),
                    onIntent = {},
                )
            }
        }
    }
}
