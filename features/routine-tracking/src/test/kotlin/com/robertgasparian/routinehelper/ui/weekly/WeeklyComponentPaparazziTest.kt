package com.robertgasparian.routinehelper.ui.weekly

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.runtime.Composable
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import org.junit.Rule
import org.junit.Test

class WeeklyComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                WeeklyComponent(uiState = RoutineTrackingUiState.preview().copy(date = "2026-05-24"))
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                WeeklyComponent(uiState = RoutineTrackingUiState.previewEmpty().copy(date = "2026-05-24"))
            }
        }
    }
}

@Composable
private fun WeeklyComponent(uiState: RoutineTrackingUiState) {
    RoutineTrackingComponent(
        uiState = uiState,
        onIntent = {},
        cadence = RoutineCadence.Weekly,
        showSnapshotAction = false,
    )
}
