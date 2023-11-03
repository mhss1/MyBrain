package com.mhss.app.mybrain.presentation.diary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.use_case.diary.*
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.SaveSettingsUseCase
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val addEntry: AddDiaryEntryUseCase,
    private val updateEntry: UpdateDiaryEntryUseCase,
    private val deleteEntry: DeleteDiaryEntryUseCase,
    private val getAlEntries: GetAllEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getEntry: GetDiaryEntryUseCase,
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val getEntriesForChart: GetDiaryForChartUseCase
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private var getEntriesJob: Job? = null

    init {
        viewModelScope.launch {
            getSettings(
                intPreferencesKey(Constants.DIARY_ORDER_KEY),
                Order.DateModified(OrderType.ASC()).toInt()
            ).collect {
                getEntries(it.toOrder())
            }
        }
    }

    fun onEvent(event: DiaryEvent) {
        when (event) {
            is DiaryEvent.AddEntry -> viewModelScope.launch {
                addEntry(event.entry)
                uiState = uiState.copy(
                    navigateUp = true
                )
            }
            is DiaryEvent.DeleteEntry -> viewModelScope.launch {
                deleteEntry(event.entry)
                uiState = uiState.copy(
                    navigateUp = true
                )
            }
            is DiaryEvent.GetEntry -> viewModelScope.launch {
                val entry = getEntry(event.entryId)
                uiState = uiState.copy(
                    entry = entry
                )
            }
            is DiaryEvent.SearchEntries -> viewModelScope.launch {
                val entries = searchEntries(event.query)
                uiState = uiState.copy(
                    searchEntries = entries
                )
            }
            is DiaryEvent.UpdateEntry -> viewModelScope.launch {
                updateEntry(event.entry)
                uiState = uiState.copy(
                    navigateUp = true
                )
            }
            is DiaryEvent.UpdateOrder -> viewModelScope.launch {
                saveSettings(
                    intPreferencesKey(Constants.DIARY_ORDER_KEY),
                    event.order.toInt()
                )
            }
            DiaryEvent.ErrorDisplayed -> uiState = uiState.copy(error = null)
            is DiaryEvent.ChangeChartEntriesRange -> viewModelScope.launch {
                uiState = uiState.copy(chartEntries = getEntriesForChart(event.monthly))
            }
            is DiaryEvent.ToggleReadingMode -> uiState = uiState.copy(readingMode = !uiState.readingMode)
        }
    }

    data class UiState(
        val entries: List<DiaryEntry> = emptyList(),
        val entriesOrder: Order = Order.DateModified(OrderType.ASC()),
        val entry: DiaryEntry? = null,
        val error: String? = null,
        val searchEntries: List<DiaryEntry> = emptyList(),
        val navigateUp: Boolean = false,
        val chartEntries : List<DiaryEntry> = emptyList(),
        val readingMode: Boolean = true
    )

    private fun getEntries(order: Order) {
        getEntriesJob?.cancel()
        getEntriesJob = getAlEntries(order)
            .onEach { entries ->
                uiState = uiState.copy(
                    entries = entries,
                    entriesOrder = order
                )
            }.launchIn(viewModelScope)
    }

}