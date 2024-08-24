package com.mhss.app.data

import com.mhss.app.domain.di.DiaryDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class DiaryDataModule

val diaryDataModule = module {
    includes(DiaryDataModule().module, DiaryDomainModule().module)
}