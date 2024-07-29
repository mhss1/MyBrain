package com.mhss.app.preferences.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class PreferenceRepositoryImpl(
    private val preferences: DataStore<Preferences>,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {

    override suspend fun <T> savePreference(key: PrefsKey<T>, value: T) {
        withContext(ioDispatcher) {
            preferences.edit { settings ->
                val dataStoreKey = key.toDatastoreKey()
                if (settings[dataStoreKey] != value)
                    settings[dataStoreKey] = value
            }
        }
    }

    override fun <T> getPreference(key: PrefsKey<T>, defaultValue: T): Flow<T> {
        return preferences.data.map { preferences -> preferences[key.toDatastoreKey()] ?: defaultValue }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> PrefsKey<T>.toDatastoreKey(): Preferences.Key<T> {
    return when (this) {
        is PrefsKey.IntKey -> intPreferencesKey(name)
        is PrefsKey.BooleanKey -> booleanPreferencesKey(name)
        is PrefsKey.StringSetKey -> stringSetPreferencesKey(name)
        is PrefsKey.StringKey -> stringPreferencesKey(name)
    } as Preferences.Key<T>
}