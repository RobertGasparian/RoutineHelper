package com.robertgasparian.routinehelper.ui.dsm

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
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
                    note = "08:30 Walked before breakfast.",
                    onNoteChange = {},
                    onDismiss = {},
                    onSaveClick = {},
                    title = "Edit note",
                    supportingText = "Daily note for Walk",
                    placeholder = "Daily note",
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
                    note = "",
                    onNoteChange = {},
                    onDismiss = {},
                    onSaveClick = {},
                    title = "Add note",
                    supportingText = "This note is saved for this day only.",
                    placeholder = "Day note",
                    autoFocus = false,
                )
            }
        }
    }
}
