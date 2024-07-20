package com.mhss.app.mybrain.di

import com.mhss.app.mybrain.dataStore
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { androidContext().dataStore }
    single { OkHttp.create() }
}