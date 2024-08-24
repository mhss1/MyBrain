package com.mhss.app.data.di

import com.mhss.app.domain.di.AiDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class AiDataModule

val aiDataModule = module {
    includes(AiDataModule().module, AiDomainModule().module)
}