package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setDailySummaryNotificationEnabled(isEnabled: Boolean)

    suspend fun setWeeklySummaryNotificationEnabled(isEnabled: Boolean)
}
