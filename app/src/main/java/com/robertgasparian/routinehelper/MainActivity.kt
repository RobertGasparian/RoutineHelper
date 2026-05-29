package com.robertgasparian.routinehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.app.RoutineHelperComponent
import com.robertgasparian.routinehelper.ui.app.TodayDestination
import com.robertgasparian.routinehelper.ui.app.RoutineHelperScreen
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
    RoutineHelperScreen()
}

@Preview(showBackground = true)
@Composable
private fun RoutineHelperAppPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            backStack = listOf(TodayDestination),
            selectedDestination = TodayDestination,
            onDestinationSelected = {},
        )
    }
}
