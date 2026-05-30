package com.robertgasparian.routinehelper.ui.weekly

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.runtime.Composable
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.today.TodayComponent
import com.robertgasparian.routinehelper.ui.today.TodayUiState
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
                WeeklyComponent(uiState = TodayUiState.preview().copy(date = "Week of 2026-05-24"))
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                WeeklyComponent(uiState = TodayUiState.previewEmpty().copy(date = "Week of 2026-05-24"))
            }
        }
    }
}

@Composable
private fun WeeklyComponent(uiState: TodayUiState) {
    TodayComponent(
        uiState = uiState,
        onEvent = {},
        title = "Weekly",
        emptyTitle = "No weekly items yet",
        emptyDescription = "Add your first weekly action to start tracking this week.",
        showSnapshotAction = false,
    )
}
