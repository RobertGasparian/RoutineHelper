package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.robertgasparian.routinehelper.ui.history.HistoryScreen
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.today.TodayScreen

@Composable
fun RoutineHelperScreen() {
    val backStack = remember { mutableStateListOf<Any>(TodayDestination) }
    val selectedDestination = backStack.lastOrNull().topLevelDestination()

    RoutineHelperComponent(
        backStack = backStack,
        selectedDestination = selectedDestination,
        onDestinationSelected = { destination ->
            if (selectedDestination != destination) {
                backStack.clear()
                backStack.add(destination)
            }
        },
    )
}

@Composable
fun RoutineHelperComponent(
    backStack: List<Any>,
    selectedDestination: Any,
    onDestinationSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            FloatingBottomNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(bottom = innerPadding.calculateBottomPadding())),
            onBack = {},
            entryProvider = { key ->
                when (key) {
                    TodayDestination -> NavEntry(key) {
                        TodayScreen()
                    }

                    HistoryDestination -> NavEntry(key) {
                        HistoryScreen()
                    }

                    else -> error("Unknown destination: $key")
                }
            },
        )
    }
}

@Composable
private fun FloatingBottomNavigationBar(
    selectedDestination: Any,
    onDestinationSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 420.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            ShortNavigationBar(
                containerColor = Color.Transparent,
            ) {
                ShortNavigationBarItem(
                    selected = selectedDestination == TodayDestination,
                    onClick = { onDestinationSelected(TodayDestination) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = "Today") },
                )
                ShortNavigationBarItem(
                    selected = selectedDestination == HistoryDestination,
                    onClick = { onDestinationSelected(HistoryDestination) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = "History") },
                )
            }
        }
    }
}

private fun Any?.topLevelDestination(): Any =
    when (this) {
        HistoryDestination -> HistoryDestination
        else -> TodayDestination
    }

@Preview(showBackground = true)
@Composable
private fun RoutineHelperComponentPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            backStack = listOf(TodayDestination),
            selectedDestination = TodayDestination,
            onDestinationSelected = {},
        )
    }
}
