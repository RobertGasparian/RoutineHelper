package com.robertgasparian.routinehelper.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.robertgasparian.routinehelper.features.settings.R
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val appLanguage = remember(configuration) {
        AppLanguage.fromLocaleList(AppCompatDelegate.getApplicationLocales())
    }

    SettingsComponent(
        uiState = uiState.copy(appLanguage = appLanguage),
        onIntent = { intent ->
            when (intent) {
                SettingsIntent.BackClick -> onBackClick()
                is SettingsIntent.AppLanguageChange -> {
                    AppCompatDelegate.setApplicationLocales(intent.appLanguage.toLocaleList())
                }
                else -> viewModel.onIntent(intent)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComponent(
    uiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLanguageDialogVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onIntent(SettingsIntent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.settings_title))
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsSection(
                title = stringResource(R.string.settings_language),
            ) {
                SettingsLanguageRow(
                    appLanguage = uiState.appLanguage,
                    onClick = { isLanguageDialogVisible = true },
                )
            }

            SettingsSection(
                title = stringResource(R.string.settings_notifications),
            ) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_daily_summary_notification),
                    isChecked = uiState.isDailySummaryNotificationEnabled,
                    onCheckedChange = { isEnabled ->
                        onIntent(SettingsIntent.DailySummaryNotificationChange(isEnabled))
                    },
                )
                HorizontalDivider()
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_weekly_summary_notification),
                    isChecked = uiState.isWeeklySummaryNotificationEnabled,
                    onCheckedChange = { isEnabled ->
                        onIntent(SettingsIntent.WeeklySummaryNotificationChange(isEnabled))
                    },
                )
            }
        }
    }

    if (isLanguageDialogVisible) {
        AppLanguageDialog(
            selectedLanguage = uiState.appLanguage,
            onLanguageSelected = { appLanguage ->
                isLanguageDialogVisible = false
                onIntent(SettingsIntent.AppLanguageChange(appLanguage))
            },
            onDismiss = { isLanguageDialogVisible = false },
        )
    }
}

@Composable
private fun SettingsLanguageRow(
    appLanguage: AppLanguage,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(text = stringResource(R.string.settings_app_language))
        },
        supportingContent = {
            Text(text = stringResource(appLanguage.displayNameRes))
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun AppLanguageDialog(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.settings_app_language))
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { appLanguage ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = appLanguage == selectedLanguage,
                                onClick = { onLanguageSelected(appLanguage) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(appLanguage.displayNameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        RadioButton(
                            selected = appLanguage == selectedLanguage,
                            onClick = null,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_cancel))
            }
        },
    )
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
        headlineContent = {
            Text(text = title)
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = null,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsComponentPreview() {
    RoutineHelperTheme {
        SettingsComponent(
            uiState = SettingsUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsComponentDarkPreview() {
    RoutineHelperTheme {
        SettingsComponent(
            uiState = SettingsUiState.previewNotificationsEnabled(),
            onIntent = {},
        )
    }
}
