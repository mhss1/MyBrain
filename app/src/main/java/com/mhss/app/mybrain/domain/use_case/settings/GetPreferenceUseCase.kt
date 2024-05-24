package com.mhss.app.mybrain.domain.use_case.settings

import com.mhss.app.mybrain.domain.model.preferences.PrefsKey
import com.mhss.app.mybrain.domain.repository.preferences.PreferenceRepository
import org.koin.core.annotation.Single

@Single
class GetPreferenceUseCase(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun <T> invoke(key: PrefsKey<T>, defaultValue: T) = preferenceRepository.getPreference(key, defaultValue)
}