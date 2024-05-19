package com.mhss.app.mybrain.domain.use_case.settings

import androidx.datastore.preferences.core.Preferences
import com.mhss.app.mybrain.domain.repository.SettingsRepository
import org.koin.core.annotation.Single

@Single
class SaveSettingsUseCase(
  private val settingsRepository: SettingsRepository
) {
    suspend operator fun <T> invoke(key: Preferences.Key<T>, value: T) = settingsRepository.saveSettings(key, value)
}