package com.mhss.app.mybrain.domain.model.preferences

import kotlin.reflect.KClass

data class PrefsKey<T> internal constructor(val name: String, val type: KClass<*>)

fun intPreferencesKey(name: String) = PrefsKey<Int>(name, Int::class)

fun booleanPreferencesKey(name: String) = PrefsKey<Boolean>(name, Boolean::class)

fun stringSetPreferencesKey(name: String) = PrefsKey<Set<String>>(name, Set::class)