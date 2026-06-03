package com.robertgasparian.routinehelper.ui.app

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoutineHelperComponent(
    topLevelBackStack: TopLevelBackStack<Any>,
    modifier: Modifier = Modifier,
) {
    val currentDestination = topLevelBackStack.backStack.lastOrNull()
    val showBottomNavigation = currentDestination is TopLevelDestination
    var navigationTransitionDirection by remember { mutableStateOf(HorizontalDirection.Right) }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        if (destination == topLevelBackStack.topLevelKey) return

        val fromIndex = topLevelBackStack.topLevelKey.topLevelTabIndex ?: 0
        val toIndex = destination.topLevelTabIndex ?: fromIndex
        navigationTransitionDirection = if (toIndex > fromIndex) {
            HorizontalDirection.Left
        } else {
            HorizontalDirection.Right
        }
        topLevelBackStack.addTopLevel(destination)
    }

    fun navigateToDetail(destination: Any) {
        navigationTransitionDirection = HorizontalDirection.Right
        topLevelBackStack.add(destination)
    }

    fun navigateBack(): Boolean {
        navigationTransitionDirection = HorizontalDirection.Left
        return topLevelBackStack.removeLast()
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNavigation,
                enter = slideInVertically(initialOffsetY = { height -> height }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { height -> height }) + fadeOut(),
            ) {
                FloatingBottomNavigationBar(
                    selectedDestination = topLevelBackStack.topLevelKey,
                    onDestinationSelected = ::navigateToTopLevel,
                )
            }
        },
    ) { _ ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigateBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                horizontalSlideContentTransform(direction = navigationTransitionDirection)
            },
            popTransitionSpec = {
                horizontalSlideContentTransform(direction = navigationTransitionDirection)
            },
            entryProvider = entryProvider {
                entry<DailyDestination> {
                    DailyScreen(
                        onCreateActionClick = {
                            navigateToDetail(ActionEditorDestination())
                        },
                        onEditActionClick = { actionId ->
                            navigateToDetail(ActionEditorDestination(actionId))
                        },
                    )
                }

                entry<WeeklyDestination> {
                    WeeklyScreen(
                        onCreateActionClick = {
                            navigateToDetail(
                                ActionEditorDestination(cadence = RoutineCadence.Weekly),
                            )
                        },
                        onEditActionClick = { actionId ->
                            navigateToDetail(
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
                                navigateToDetail(HistoryDetailDestination(snapshotId))
                            },
                        )
                    }

                entry<HistoryDetailDestination> { destination ->
                        HistoryDetailScreen(
                            snapshotId = destination.snapshotId,
                            onBackClick = { navigateBack() },
                        )
                }

                entry<ActionEditorDestination> { destination ->
                    ActionEditorScreen(
                        actionId = destination.actionId,
                        cadence = destination.cadence,
                        onBackClick = { navigateBack() },
                    )
                }
            },
        )
    }
}

private fun horizontalSlideContentTransform(
    direction: HorizontalDirection,
): ContentTransform {
    val sign = if (direction == HorizontalDirection.Left) 1 else -1
    return slideInHorizontally { width -> sign * width } togetherWith
        slideOutHorizontally { width -> -sign * width }
}

private val Any?.topLevelTabIndex: Int?
    get() = TopLevelNavigationItems.indexOfFirst { item -> item.destination == this }
        .takeIf { index -> index >= 0 }

private enum class HorizontalDirection {
    Left,
    Right,
}

@Composable
private fun FloatingBottomNavigationBar(
    selectedDestination: Any,
    onDestinationSelected: (TopLevelDestination) -> Unit,
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
                TopLevelNavigationItems.forEach { item ->
                    ShortNavigationBarItem(
                        selected = selectedDestination == item.destination,
                        onClick = { onDestinationSelected(item.destination) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(text = item.label) },
                    )
                }
            }
        }
    }
}

private data class TopLevelNavigationItem(
    val destination: TopLevelDestination,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val TopLevelNavigationItems = listOf(
    TopLevelNavigationItem(
        destination = DailyDestination,
        label = "Daily",
        icon = Icons.Default.Event,
    ),
    TopLevelNavigationItem(
        destination = WeeklyDestination,
        label = "Weekly",
        icon = Icons.Default.ViewWeek,
    ),
    TopLevelNavigationItem(
        destination = HistoryDestination,
        label = "History",
        icon = Icons.Default.History,
    ),
)

@Preview(showBackground = true)
@Composable
private fun RoutineHelperComponentPreview() {
    RoutineHelperTheme {
        RoutineHelperComponent(
            topLevelBackStack = TopLevelBackStack<Any>(DailyDestination),
        )
    }
}
