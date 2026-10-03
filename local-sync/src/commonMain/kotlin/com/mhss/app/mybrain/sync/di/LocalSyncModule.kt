package com.mhss.app.mybrain.sync.di

import com.mhss.app.mybrain.sync.util.EncryptionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.mhss.app.mybrain.sync")
class LocalSyncModule {

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    @Single
    fun provideEncryptionManager(): EncryptionManager = EncryptionManager()

    @Single
    fun provideHttpClient(): HttpClient = HttpClient {
        install(WebSockets.Plugin) {
            pingIntervalMillis = 10000L
        }
    }
}
