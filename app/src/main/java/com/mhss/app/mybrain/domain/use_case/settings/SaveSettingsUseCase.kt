package com.mhss.app.mybrain.domain.use_case.settings

import androidx.datastore.preferences.core.Preferences
import com.mhss.app.mybrain.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {
    suspend operator fun <T> invoke(key: Preferences.Key<T>, value: T) = settingsRepository.saveSettings(key, value)
}