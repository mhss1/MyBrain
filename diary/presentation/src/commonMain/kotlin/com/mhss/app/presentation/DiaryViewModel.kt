package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.GetAllEntriesUseCase
import com.mhss.app.domain.use_case.GetDiaryForChartUseCase
import com.mhss.app.domain.use_case.SearchEntriesUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.inTheLast30Days
import com.mhss.app.datetime.inTheLastYear
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class DiaryViewModel(
    private val getAlEntries: GetAllEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getEntriesForChart: GetDiaryForChartUseCase,
    private val dateTimeFormatter: DateTimeFormatter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private var getEntriesJob: Job? = null

    init {
        viewModelScope.launch {
            getPreference(
                intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                SortOrder.DateCreated(SortType.DESC).toInt()
            ).collect {
                getEntries(it.toOrder())
            }
        }
    }

    fun onEvent(event: DiaryEvent) {
        when (event) {
            is DiaryEvent.SearchEntries -> viewModelScope.launch {
                val entries = searchEntries(event.query)
                uiState = uiState.copy(
                    searchEntries = entries
                )
            }
            is DiaryEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                    event.sortOrder.toInt()
                )
            }
            is DiaryEvent.ChangeChartEntriesRange -> viewModelScope.launch {
                uiState = uiState.copy(chartEntries = getEntriesForChart {
                    if (event.monthly) it.createdDate.inTheLast30Days()
                    else it.createdDate.inTheLastYear()
                })
            }
        }
    }

    data class UiState(
        val entries: Map<String, List<DiaryEntry>> = emptyMap(),
        val entriesSortOrder: SortOrder = SortOrder.DateCreated(SortType.DESC),
        val searchEntries: List<DiaryEntry> = emptyList(),
        val chartEntries : List<DiaryEntry> = emptyList()
    )

    private fun getEntries(sortOrder: SortOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = getAlEntries(sortOrder)
            .onEach { entries ->
                uiState = uiState.copy(
                    entries = entries.groupBy {
                        dateTimeFormatter.formatDateForMapping(it.createdDate)
                    },
                    entriesSortOrder = sortOrder
                )
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

}