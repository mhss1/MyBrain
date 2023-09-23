package com.mhss.app.mybrain.di

import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@EntryPoint
@InstallIn(SingletonComponent::class)
interface CalendarWidgetEntryPoint {

    fun getSettingsUseCase(): GetSettingsUseCase

    fun getAllEventsUseCase(): GetAllEventsUseCase
}
