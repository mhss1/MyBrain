package com.mhss.app.mybrain.di

import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetAllTasksUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TasksWidgetEntryPoint {
    fun getSettingsUseCase(): GetSettingsUseCase

    fun getAllTasksUseCase(): GetAllTasksUseCase
}