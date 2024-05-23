package com.mhss.app.mybrain.domain.repository.widget


interface WidgetUpdater {
    suspend fun updateAll(type: WidgetType)

    sealed interface WidgetType {
        data object Calendar : WidgetType
        data object Tasks : WidgetType
    }
}