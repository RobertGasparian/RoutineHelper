package com.robertgasparian.routinehelper.ui.settings

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.AppSettings
import com.robertgasparian.routinehelper.domain.usecase.AppSettingsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetDailySummaryNotificationEnabledUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklySummaryNotificationEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appSettingsUseCase: AppSettingsUseCase,
    private val setDailySummaryNotificationEnabledUseCase: SetDailySummaryNotificationEnabledUseCase,
    private val setWeeklySummaryNotificationEnabledUseCase: SetWeeklySummaryNotificationEnabledUseCase,
) : BaseViewModel<SettingsUiState, SettingsIntent, Nothing>() {
    override val uiState: StateFlow<SettingsUiState> =
        appSettingsUseCase()
            .map(AppSettings::toUiState)
            .stateInViewModel(initialValue = SettingsUiState())

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.BackClick,
            is SettingsIntent.AppLanguageChange -> Unit
            is SettingsIntent.DailySummaryNotificationChange -> launch {
                setDailySummaryNotificationEnabledUseCase(intent.isEnabled)
            }
            is SettingsIntent.WeeklySummaryNotificationChange -> launch {
                setWeeklySummaryNotificationEnabledUseCase(intent.isEnabled)
            }
        }
    }
}

private fun AppSettings.toUiState(): SettingsUiState =
    SettingsUiState(
        isDailySummaryNotificationEnabled = isDailySummaryNotificationEnabled,
        isWeeklySummaryNotificationEnabled = isWeeklySummaryNotificationEnabled,
    )
