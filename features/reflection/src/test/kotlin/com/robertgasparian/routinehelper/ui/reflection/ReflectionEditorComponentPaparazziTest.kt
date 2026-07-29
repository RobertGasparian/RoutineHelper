package com.robertgasparian.routinehelper.ui.reflection

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorState
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class ReflectionEditorComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun `given initialized Reflection state when rendered then editor is shown`() {
        paparazzi.snapshot {
            RoutineHelperTheme(
                darkTheme = false,
                dynamicColor = false,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    ReflectionEditorComponent(
                        state = ReflectionEditorState.preview(),
                        onIntent = {},
                        autoFocus = false,
                    )
                }
            }
        }
    }
}
