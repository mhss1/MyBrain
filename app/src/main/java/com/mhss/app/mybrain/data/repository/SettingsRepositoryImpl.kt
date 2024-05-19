package com.mhss.app.mybrain.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class SettingsRepositoryImpl(
    private val preferences: DataStore<Preferences>,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {

    override suspend fun <T> saveSettings(key: Preferences.Key<T>, value: T) {
        withContext(ioDispatcher) {
            preferences.edit { settings ->
                if (settings[key] != value)
                    settings[key] = value
            }
        }
    }

    override fun <T> getSettings(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return preferences.data.map { preferences -> preferences[key] ?: defaultValue }
    }
}