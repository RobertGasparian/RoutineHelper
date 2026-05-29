package com.robertgasparian.routinehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.today.TodayComponent
import com.robertgasparian.routinehelper.ui.today.TodayScreen
import com.robertgasparian.routinehelper.ui.today.TodayUiState
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoutineHelperTheme {
                RoutineHelperApp()
            }
        }
    }
}

@Composable
private fun RoutineHelperApp() {
    TodayScreen()
}

@Preview(showBackground = true)
@Composable
private fun RoutineHelperAppPreview() {
    RoutineHelperTheme {
        TodayComponent(
            uiState = TodayUiState.preview(),
            onAddAction = { _, _ -> },
            onCheckedChange = { _, _ -> },
            onNoteChange = { _, _ -> },
        )
    }
}
