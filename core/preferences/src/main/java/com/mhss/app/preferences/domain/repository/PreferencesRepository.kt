package com.mhss.app.preferences.domain.repository

import com.mhss.app.preferences.domain.model.PrefsKey
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    suspend fun <T> savePreference(key: PrefsKey<T>, value: T)

    fun <T> getPreference(key: PrefsKey<T>, defaultValue: T): Flow<T>

}