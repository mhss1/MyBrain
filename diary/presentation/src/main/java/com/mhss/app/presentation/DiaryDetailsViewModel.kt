package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.*
import com.mhss.app.util.date.now
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DiaryDetailsViewModel(
    private val getEntry: GetDiaryEntryUseCase,
    private val addEntry: AddDiaryEntryUseCase,
    private val updateEntry: UpdateDiaryEntryUseCase,
    private val deleteEntry: DeleteDiaryEntryUseCase,
    entryId: Int
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        viewModelScope.launch {
            if (entryId != -1) {
                uiState = uiState.copy(
                    entry = getEntry(entryId),
                    readingMode = true
                )
            }
        }
    }

    fun onEvent(event: DiaryDetailsEvent) {
        when (event) {
            is DiaryDetailsEvent.DeleteEntry -> viewModelScope.launch {
                deleteEntry(uiState.entry!!)
                uiState = uiState.copy(navigateUp = true)
            }
            is DiaryDetailsEvent.ToggleReadingMode -> {
                uiState = uiState.copy(readingMode = !uiState.readingMode)
            }
            is DiaryDetailsEvent.ScreenOnStop -> viewModelScope.launch {
                if (uiState.entry == null) {
                    if (event.currentEntry.title.isNotBlank() || event.currentEntry.content.isNotBlank()) {
                        val entry = event.currentEntry.copy(
                            updatedDate = now()
                        )
                        val id = addEntry(entry)
                        uiState = uiState.copy(entry = entry.copy(id = id.toInt()))
                    }
                } else if (entryChanged(uiState.entry!!, event.currentEntry)) {
                    val newEntry = uiState.entry!!.copy(
                        title = event.currentEntry.title,
                        content = event.currentEntry.content,
                        mood = event.currentEntry.mood,
                        createdDate = event.currentEntry.createdDate,
                        updatedDate = now()
                    )
                    updateEntry(newEntry)
                    uiState = uiState.copy(entry = newEntry)
                }
            }
        }
    }

    private fun entryChanged(entry: DiaryEntry, newEntry: DiaryEntry): Boolean {
        return entry.title != newEntry.title ||
                entry.content != newEntry.content ||
                entry.mood != newEntry.mood
    }

    data class UiState(
        val entry: DiaryEntry? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false
    )
}