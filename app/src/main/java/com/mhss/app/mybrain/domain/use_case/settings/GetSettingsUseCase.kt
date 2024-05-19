package com.mhss.app.mybrain.domain.use_case.settings

import androidx.datastore.preferences.core.Preferences
import com.mhss.app.mybrain.domain.repository.SettingsRepository
import org.koin.core.annotation.Single

@Single
class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun <T> invoke(key: Preferences.Key<T>, defaultValue: T) = settingsRepository.getSettings(key, defaultValue)
}