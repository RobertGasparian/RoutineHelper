package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

internal val RoutineDestination?.topLevelTabIndex: Int?
    get() = TopLevelNavigationItems.indexOfFirst { item -> item.destination == this }
        .takeIf { index -> index >= 0 }

@Composable
internal fun FloatingBottomNavigationBar(
    selectedDestination: RoutineDestination,
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
    val icon: ImageVector,
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
