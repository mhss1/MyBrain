package com.mhss.app.presentation

import com.mhss.app.preferences.domain.model.SortOrder

sealed class DiaryEvent {
    data class SearchEntries(val query: String) : DiaryEvent()
    data class UpdateOrder(val sortOrder: SortOrder) : DiaryEvent()
    data class ChangeChartEntriesRange(val monthly: Boolean) : DiaryEvent()
}