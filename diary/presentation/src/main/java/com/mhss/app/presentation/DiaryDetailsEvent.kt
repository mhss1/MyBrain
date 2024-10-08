package com.mhss.app.presentation

import com.mhss.app.domain.model.DiaryEntry

sealed class DiaryDetailsEvent {
    data object DeleteEntry : DiaryDetailsEvent()
    data object ToggleReadingMode : DiaryDetailsEvent()
    data class ScreenOnStop(val currentEntry: DiaryEntry) : DiaryDetailsEvent()
}