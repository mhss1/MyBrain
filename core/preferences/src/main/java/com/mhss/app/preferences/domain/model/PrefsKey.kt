package com.mhss.app.preferences.domain.model

sealed class PrefsKey<T>(val name: String) {
    class IntKey(name: String): PrefsKey<Int>(name)
    class BooleanKey(name: String): PrefsKey<Boolean>(name)
    class StringSetKey(name: String): PrefsKey<Set<String>>(name)
}
fun intPreferencesKey(name: String) = PrefsKey.IntKey(name)
fun booleanPreferencesKey(name: String) = PrefsKey.BooleanKey(name)
fun stringSetPreferencesKey(name: String) = PrefsKey.StringSetKey(name)