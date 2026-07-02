package com.robertgasparian.routinehelper.ui.settings

data class SettingsUiState(
    val isDailySummaryNotificationEnabled: Boolean = false,
    val isWeeklySummaryNotificationEnabled: Boolean = false,
) {
    companion object {
        fun preview(): SettingsUiState =
            SettingsUiState()

        fun previewNotificationsEnabled(): SettingsUiState =
            SettingsUiState(
                isDailySummaryNotificationEnabled = true,
                isWeeklySummaryNotificationEnabled = true,
            )
    }
}
