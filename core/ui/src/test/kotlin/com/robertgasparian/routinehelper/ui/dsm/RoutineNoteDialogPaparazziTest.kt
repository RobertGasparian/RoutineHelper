package com.robertgasparian.routinehelper.ui.dsm

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.ui.text.input.TextFieldValue
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class RoutineNoteDialogPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                RoutineNoteDialogContent(
                    value = TextFieldValue("08:30 Walked before breakfast."),
                    onIntent = {},
                    title = "Edit note",
                    supportingText = "Daily note for Walk",
                    label = "Daily note",
                    autoFocus = false,
                )
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                RoutineNoteDialogContent(
                    value = TextFieldValue(""),
                    onIntent = {},
                    title = "Add note",
                    supportingText = "This note is saved for this day only.",
                    label = "Day note",
                    autoFocus = false,
                )
            }
        }
    }
}
