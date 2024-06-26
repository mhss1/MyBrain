package com.mhss.app.di

import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val namedIoDispatcher = "ioDispatcher"
const val namedDefaultDispatcher = "defaultDispatcher"

val coroutinesModule = module {
    single(named(namedDefaultDispatcher)) { Dispatchers.Default }
    single(named(namedIoDispatcher)) { Dispatchers.IO }
}