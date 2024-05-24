package com.mhss.app.mybrain.domain.repository.preferences

import com.mhss.app.mybrain.domain.model.preferences.PrefsKey
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    suspend fun <T> savePreference(key: PrefsKey<T>, value: T)

    fun <T> getPreference(key: PrefsKey<T>, defaultValue: T): Flow<T>

}