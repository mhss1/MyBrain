package com.mhss.app.data

import com.mhss.app.domain.di.TasksDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class TasksDataModule

val tasksDataModule = module {
    includes(TasksDataModule().module, TasksDomainModule().module)
}