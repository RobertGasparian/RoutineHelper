package com.robertgasparian.routinehelper.ui.dsm

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class RoutineSwipeToDismissPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given settled state when rendered then dismiss action remains behind content`() {
        paparazzi.snapshot {
            RoutineSwipeToDismissPreviewContent()
        }
    }
}
