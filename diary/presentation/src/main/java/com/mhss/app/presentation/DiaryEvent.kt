package com.mhss.app.presentation

import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.preferences.domain.model.Order

sealed class DiaryEvent {
    data class AddEntry(val entry: DiaryEntry) : DiaryEvent()
    data class GetEntry(val entryId: Int) : DiaryEvent()
    data class SearchEntries(val query: String) : DiaryEvent()
    data class UpdateOrder(val order: Order) : DiaryEvent()
    data class UpdateEntry(val entry: DiaryEntry) : DiaryEvent()
    data class DeleteEntry(val entry: DiaryEntry) : DiaryEvent()
    data class ChangeChartEntriesRange(val monthly: Boolean) : DiaryEvent()
    data object ErrorDisplayed: DiaryEvent()
    data object ToggleReadingMode: DiaryEvent()
}