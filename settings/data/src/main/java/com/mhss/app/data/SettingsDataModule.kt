package com.mhss.app.data

import com.mhss.app.domain.di.SettingsDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class SettingsDataModule

val settingsDataModule = module {
    includes(SettingsDataModule().module, SettingsDomainModule().module)
}