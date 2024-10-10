package com.mhss.app.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coroutinesModule = module {
    single(named("defaultDispatcher")) { Dispatchers.Default }
    single(named("ioDispatcher")) { Dispatchers.IO }
    single(named("applicationScope")) { CoroutineScope(SupervisorJob()) }
}