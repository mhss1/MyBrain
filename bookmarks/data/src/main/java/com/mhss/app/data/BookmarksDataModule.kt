package com.mhss.app.data

import com.mhss.app.domain.di.BookmarksDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class BookmarksDataModule

val bookmarksDataModule = module {
    includes(BookmarksDataModule().module, BookmarksDomainModule().module)
}