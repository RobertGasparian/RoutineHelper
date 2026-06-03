package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.actioneditor.ActionEditorScreen
import com.robertgasparian.routinehelper.ui.history.detail.HistoryDetailScreen
import com.robertgasparian.routinehelper.ui.history.HistoryScreen
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.daily.DailyScreen
import com.robertgasparian.routinehelper.ui.weekly.WeeklyScreen

@Composable
fun RoutineHelperScreen() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(DailyDestination) }

    RoutineHelperComponent(
        topLevelBackStack = topLevelBackStack,
    )
}

@Composable
fun RoutineHelperComponent(
    topLevelBackStack: TopLevelBackStack<Any>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            FloatingBottomNavigationBar(
                selectedDestination = topLevelBackStack.topLevelKey,
                onDestinationSelected = topLevelBackStack::addTopLevel,
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<DailyDestination> {
                        DailyScreen(
                            onCreateActionClick = {
                                topLevelBackStack.add(ActionEditorDestination())
                            },
                            onEditActionClick = { actionId ->
                                topLevelBackStack.add(ActionEditorDestination(actionId))
                            },
                        )
                    }

                entry<WeeklyDestination> {
                        WeeklyScreen(
                            onCreateActionClick = {
                                topLevelBackStack.add(
                                    ActionEditorDestination(cadence = RoutineCadence.Weekly),
                                )
                            },
                            onEditActionClick = { actionId ->
                                topLevelBackStack.add(
                                    ActionEditorDestination(
                                        actionId = actionId,
                                        cadence = RoutineCadence.Weekly,
                                    ),
                                )
                            },
                        )
                    }

                entry<HistoryDestination> {
                        HistoryScreen(
                            onSnapshotClick = { snapshotId ->
                                topLevelBackStack.add(HistoryDetailDestination(snapshotId))
                            },
                        )
                    }

                entry<HistoryDetailDestination> { destination ->
                        HistoryDetailScreen(
                            snapshotId = destination.snapshotId,
                            onBackClick = { topLevelBackStack.removeLast() },
                        )
                }

                entry<ActionEditorDestination> { destination ->
                    ActionEditorScreen(
                        actionId = destination.actionId,
                        cadence = destination.cadence,
                        onBackClick = { topLevelBackStack.removeLast() },
                    )
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
                    selected = selectedDestination == DailyDestination,
                    onClick = { onDestinationSelected(DailyDestination) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = "Daily") },
                )
                ShortNavigationBarItem(
                    selected = selectedDestination == WeeklyDestination,
                    onClick = { onDestinationSelected(WeeklyDestination) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ViewWeek,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = "Weekly") },
                )
                ShortNavigationBarItem(
                    selected = selectedDestination == HistoryDestination,
                    onClick = { onDestinationSelected(HistoryDestination) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = "History") },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineHelperComponentPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            topLevelBackStack = TopLevelBackStack<Any>(DailyDestination),
        )
    }
}
