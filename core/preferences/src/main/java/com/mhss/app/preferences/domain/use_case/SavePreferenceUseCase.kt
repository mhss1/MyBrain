package com.mhss.app.preferences.domain.use_case

import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.repository.PreferenceRepository
import org.koin.core.annotation.Single

@Single
class SavePreferenceUseCase(
  private val preferenceRepository: PreferenceRepository
) {
    suspend operator fun <T> invoke(key: PrefsKey<T>, value: T) = preferenceRepository.savePreference(key, value)
}