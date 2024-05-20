package com.mhss.app.mybrain.domain.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T)

    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T>

}