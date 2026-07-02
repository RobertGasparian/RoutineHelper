package com.robertgasparian.routinehelper.ui.settings

sealed interface SettingsIntent {
    data object BackClick : SettingsIntent

    data class DailySummaryNotificationChange(
        val isEnabled: Boolean,
    ) : SettingsIntent

    data class WeeklySummaryNotificationChange(
        val isEnabled: Boolean,
    ) : SettingsIntent
}
