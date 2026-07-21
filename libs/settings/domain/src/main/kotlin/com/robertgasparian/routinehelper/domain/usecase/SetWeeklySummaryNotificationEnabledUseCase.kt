package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.SettingsRepository
import javax.inject.Inject

class SetWeeklySummaryNotificationEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(isEnabled: Boolean) {
        repository.setWeeklySummaryNotificationEnabled(isEnabled)
    }
}
