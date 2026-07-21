package com.robertgasparian.routinehelper.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.robertgasparian.routinehelper.domain.model.AppSettings
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `given no stored preferences when settings are observed then defaults are disabled`() = runTest {
        val repository = createRepository()

        assertEquals(AppSettings(), repository.settings.first())
    }

    @Test
    fun `given notification changes when settings are observed then values are persisted`() = runTest {
        val repository = createRepository()

        repository.setDailySummaryNotificationEnabled(true)
        repository.setWeeklySummaryNotificationEnabled(true)

        assertEquals(
            AppSettings(
                isDailySummaryNotificationEnabled = true,
                isWeeklySummaryNotificationEnabled = true,
            ),
            repository.settings.first(),
        )
    }

    private fun createRepository(): DataStoreSettingsRepository {
        val preferencesFile = File(temporaryFolder.root, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { preferencesFile },
        )
        return DataStoreSettingsRepository(dataStore)
    }
}
