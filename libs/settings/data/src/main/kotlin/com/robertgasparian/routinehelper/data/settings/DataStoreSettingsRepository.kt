package com.robertgasparian.routinehelper.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.robertgasparian.routinehelper.domain.model.AppSettings
import com.robertgasparian.routinehelper.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<AppSettings> =
        dataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                AppSettings(
                    isDailySummaryNotificationEnabled =
                        preferences[DailySummaryNotificationEnabledKey] ?: false,
                    isWeeklySummaryNotificationEnabled =
                        preferences[WeeklySummaryNotificationEnabledKey] ?: false,
                )
            }

    override suspend fun setDailySummaryNotificationEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DailySummaryNotificationEnabledKey] = isEnabled
        }
    }

    override suspend fun setWeeklySummaryNotificationEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WeeklySummaryNotificationEnabledKey] = isEnabled
        }
    }
}

private val DailySummaryNotificationEnabledKey =
    booleanPreferencesKey("daily_summary_notification_enabled")
private val WeeklySummaryNotificationEnabledKey =
    booleanPreferencesKey("weekly_summary_notification_enabled")
