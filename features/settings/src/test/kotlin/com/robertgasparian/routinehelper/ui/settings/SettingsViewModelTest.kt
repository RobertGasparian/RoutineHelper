package com.robertgasparian.routinehelper.ui.settings

import com.robertgasparian.routinehelper.core.testing.MainDispatcherRule
import com.robertgasparian.routinehelper.domain.model.AppSettings
import com.robertgasparian.routinehelper.domain.repository.SettingsRepository
import com.robertgasparian.routinehelper.domain.usecase.AppSettingsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetDailySummaryNotificationEnabledUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklySummaryNotificationEnabledUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    @Test
    fun `given persisted settings when state is observed then notification values are exposed`() = runTest {
        repository.setDailySummaryNotificationEnabled(true)

        val state = createViewModel().uiState.first { it.isDailySummaryNotificationEnabled }

        assertTrue(state.isDailySummaryNotificationEnabled)
        assertFalse(state.isWeeklySummaryNotificationEnabled)
    }

    @Test
    fun `given notification intents when handled then settings are persisted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.DailySummaryNotificationChange(true))
        viewModel.onIntent(SettingsIntent.WeeklySummaryNotificationChange(true))
        advanceUntilIdle()

        assertTrue(repository.value.isDailySummaryNotificationEnabled)
        assertTrue(repository.value.isWeeklySummaryNotificationEnabled)
    }

    private fun createViewModel(): SettingsViewModel =
        SettingsViewModel(
            appSettingsUseCase = AppSettingsUseCase(repository),
            setDailySummaryNotificationEnabledUseCase =
                SetDailySummaryNotificationEnabledUseCase(repository),
            setWeeklySummaryNotificationEnabledUseCase =
                SetWeeklySummaryNotificationEnabledUseCase(repository),
        )
}

private class FakeSettingsRepository : SettingsRepository {
    private val mutableSettings = MutableStateFlow(AppSettings())

    override val settings: Flow<AppSettings> = mutableSettings

    val value: AppSettings
        get() = mutableSettings.value

    override suspend fun setDailySummaryNotificationEnabled(isEnabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            isDailySummaryNotificationEnabled = isEnabled,
        )
    }

    override suspend fun setWeeklySummaryNotificationEnabled(isEnabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            isWeeklySummaryNotificationEnabled = isEnabled,
        )
    }
}
