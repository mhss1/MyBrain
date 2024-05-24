package com.mhss.app.mybrain.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.preferences.PrefsKey
import com.mhss.app.mybrain.domain.repository.preferences.PreferenceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class PreferenceRepositoryImpl(
    private val preferences: DataStore<Preferences>,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {

    override suspend fun <T> savePreference(key: PrefsKey<T>, value: T) {
        withContext(ioDispatcher) {
            preferences.edit { settings ->
                val k = key.toDatastoreKey()
                if (settings[k] != value)
                    settings[k] = value
            }
        }
    }

    override fun <T> getPreference(key: PrefsKey<T>, defaultValue: T): Flow<T> {
        return preferences.data.map { preferences -> preferences[key.toDatastoreKey()] ?: defaultValue }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> PrefsKey<T>.toDatastoreKey(): Preferences.Key<T> {
    return when (type) {
        Int::class -> intPreferencesKey(name)
        Boolean::class -> booleanPreferencesKey(name)
        Set::class -> stringSetPreferencesKey(name)
        else -> throw IllegalArgumentException("Unsupported type")
    } as Preferences.Key<T>
}

