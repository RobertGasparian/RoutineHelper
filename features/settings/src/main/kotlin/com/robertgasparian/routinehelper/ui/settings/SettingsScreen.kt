package com.robertgasparian.routinehelper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

data class SettingsUiState(
    val isDailySummaryNotificationEnabled: Boolean = false,
    val isWeeklySummaryNotificationEnabled: Boolean = false,
)

sealed interface SettingsUiEvent {
    data object BackClick : SettingsUiEvent

    data class DailySummaryNotificationChange(
        val isEnabled: Boolean,
    ) : SettingsUiEvent

    data class WeeklySummaryNotificationChange(
        val isEnabled: Boolean,
    ) : SettingsUiEvent
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDailySummaryNotificationEnabled by rememberSaveable { mutableStateOf(false) }
    var isWeeklySummaryNotificationEnabled by rememberSaveable { mutableStateOf(false) }
    val uiState = SettingsUiState(
        isDailySummaryNotificationEnabled = isDailySummaryNotificationEnabled,
        isWeeklySummaryNotificationEnabled = isWeeklySummaryNotificationEnabled,
    )

    SettingsComponent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                SettingsUiEvent.BackClick -> onBackClick()
                is SettingsUiEvent.DailySummaryNotificationChange -> {
                    isDailySummaryNotificationEnabled = event.isEnabled
                }
                is SettingsUiEvent.WeeklySummaryNotificationChange -> {
                    isWeeklySummaryNotificationEnabled = event.isEnabled
                }
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComponent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(text = "Settings")
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsSection(
                title = "Notifications",
            ) {
                SettingsSwitchRow(
                    title = "Daily Actions Summary Notification",
                    isChecked = uiState.isDailySummaryNotificationEnabled,
                    onCheckedChange = { isEnabled ->
                        onEvent(SettingsUiEvent.DailySummaryNotificationChange(isEnabled))
                    },
                )
                HorizontalDivider()
                SettingsSwitchRow(
                    title = "Weekly Actions Summary Notification",
                    isChecked = uiState.isWeeklySummaryNotificationEnabled,
                    onCheckedChange = { isEnabled ->
                        onEvent(SettingsUiEvent.WeeklySummaryNotificationChange(isEnabled))
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!isChecked) },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        headlineContent = {
            Text(text = title)
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsComponentPreview() {
    RoutineHelperTheme {
        SettingsComponent(
            uiState = SettingsUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsComponentDarkPreview() {
    RoutineHelperTheme {
        SettingsComponent(
            uiState = SettingsUiState(
                isDailySummaryNotificationEnabled = true,
                isWeeklySummaryNotificationEnabled = true,
            ),
            onEvent = {},
        )
    }
}
