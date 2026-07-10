package com.robertgasparian.routinehelper.ui.currentlist.undo

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class CurrentListUndoSnackbarHostPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given pending removals when rendered then displays both undo actions`() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CurrentListUndoSnackbarHost(
                        uiState = CurrentListUndoUiState.preview(),
                        onIntent = {},
                    )
                }
            }
        }
    }
}
