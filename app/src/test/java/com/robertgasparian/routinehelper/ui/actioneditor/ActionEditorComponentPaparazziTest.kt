package com.robertgasparian.routinehelper.ui.actioneditor

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class ActionEditorComponentPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                ActionEditorComponent(
                    uiState = ActionEditorUiState.preview(),
                    onBackClick = {},
                    onTitleChange = {},
                    onDescriptionChange = {},
                    onSaveClick = {},
                )
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                ActionEditorComponent(
                    uiState = ActionEditorUiState.previewEmpty(),
                    onBackClick = {},
                    onTitleChange = {},
                    onDescriptionChange = {},
                    onSaveClick = {},
                )
            }
        }
    }
}
