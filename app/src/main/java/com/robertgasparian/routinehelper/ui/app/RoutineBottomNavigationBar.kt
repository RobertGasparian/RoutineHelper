package com.robertgasparian.routinehelper.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
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
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.R

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
                        label = { Text(text = stringResource(item.labelRes)) },
                    )
                }
            }
        }
    }
}

private data class TopLevelNavigationItem(
    val destination: TopLevelDestination,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val TopLevelNavigationItems = listOf(
    TopLevelNavigationItem(
        destination = CurrentListDestination,
        labelRes = R.string.app_nav_list,
        icon = Icons.Default.Checklist,
    ),
    TopLevelNavigationItem(
        destination = DailyDestination,
        labelRes = R.string.app_nav_daily,
        icon = Icons.Default.Event,
    ),
    TopLevelNavigationItem(
        destination = WeeklyDestination,
        labelRes = R.string.app_nav_weekly,
        icon = Icons.Default.ViewWeek,
    ),
    TopLevelNavigationItem(
        destination = HistoryDestination,
        labelRes = R.string.app_nav_history,
        icon = Icons.Default.History,
    ),
)
