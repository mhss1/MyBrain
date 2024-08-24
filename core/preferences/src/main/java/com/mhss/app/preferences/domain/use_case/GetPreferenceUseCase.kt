package com.mhss.app.preferences.domain.use_case

import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.repository.PreferenceRepository
import org.koin.core.annotation.Single

@Single
class GetPreferenceUseCase(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun <T> invoke(key: PrefsKey<T>, defaultValue: T) = preferenceRepository.getPreference(key, defaultValue)
}