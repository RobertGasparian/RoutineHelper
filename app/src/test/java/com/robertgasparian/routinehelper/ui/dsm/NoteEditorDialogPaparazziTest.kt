package com.robertgasparian.routinehelper.ui.dsm

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import org.junit.Rule
import org.junit.Test

class NoteEditorDialogPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun populated() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                NoteEditorDialog(
                    title = "Edit note",
                    textFieldLabel = "Today note",
                    initialNote = "08:30 Walked before breakfast.",
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }
    }

    @Test
    fun empty() {
        paparazzi.snapshot {
            RoutineHelperTheme {
                NoteEditorDialog(
                    title = "Add note",
                    textFieldLabel = "Day note",
                    initialNote = "",
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }
    }
}
