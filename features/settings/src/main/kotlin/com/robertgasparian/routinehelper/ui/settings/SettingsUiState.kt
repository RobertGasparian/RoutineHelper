package com.robertgasparian.routinehelper.ui.settings

data class SettingsUiState(
    val appLanguage: AppLanguage = AppLanguage.SystemDefault,
    val isDailySummaryNotificationEnabled: Boolean = false,
    val isWeeklySummaryNotificationEnabled: Boolean = false,
) {
    companion object {
        fun preview(): SettingsUiState =
            SettingsUiState()

        fun previewNotificationsEnabled(): SettingsUiState =
            SettingsUiState(
                appLanguage = AppLanguage.Russian,
                isDailySummaryNotificationEnabled = true,
                isWeeklySummaryNotificationEnabled = true,
            )
    }
}
