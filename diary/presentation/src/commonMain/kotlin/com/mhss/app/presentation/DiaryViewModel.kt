package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mhss.app.domain.use_case.GetAllEntriesUseCase
import com.mhss.app.domain.use_case.GetDiaryForChartUseCase
import com.mhss.app.domain.use_case.SearchEntriesUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toSortOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class DiaryViewModel(
    private val getAllEntries: GetAllEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getEntriesForChart: GetDiaryForChartUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private val sortOrder = MutableStateFlow<SortOrder>(SortOrder.DateCreated(SortType.DESC))
    val entries = sortOrder.flatMapLatest { getAllEntries.paged(it) }.cachedIn(viewModelScope)

    private val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery.flatMapLatest {
        delay(300.milliseconds)
        searchEntries.paged(it)
    }.cachedIn(viewModelScope)

    private val chartRange = MutableStateFlow<Boolean?>(null)
    val chartEntries = chartRange.flatMapLatest { monthly ->
        if (monthly == null) flowOf(emptyList()) else {
            val to = Clock.System.now()
            val zone = TimeZone.currentSystemDefault()
            val from = if (monthly) to.minus(30, DateTimeUnit.DAY, zone)
                else to.minus(1, DateTimeUnit.YEAR, zone)
            getEntriesForChart(from.toEpochMilliseconds(), to.toEpochMilliseconds())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            getPreference(
                intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                SortOrder.DateCreated(SortType.DESC).toInt()
            ).collect {
                it.toSortOrder().let { order ->
                    sortOrder.value = order
                    uiState = uiState.copy(entriesSortOrder = order)
                }
            }
        }
    }

    fun onEvent(event: DiaryEvent) {
        when (event) {
            is DiaryEvent.SearchEntries -> searchQuery.value = event.query
            is DiaryEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                    event.sortOrder.toInt()
                )
            }
            is DiaryEvent.ChangeChartEntriesRange -> chartRange.value = event.monthly
        }
    }

    data class UiState(
        val entriesSortOrder: SortOrder = SortOrder.DateCreated(SortType.DESC),
    )
}
