package com.robertgasparian.routinehelper.domain.model

data class AppSettings(
    val isDailySummaryNotificationEnabled: Boolean = false,
    val isWeeklySummaryNotificationEnabled: Boolean = false,
)
