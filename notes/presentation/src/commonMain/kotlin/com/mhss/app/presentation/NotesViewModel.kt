package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mhss.app.domain.model.NoteException
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.use_case.CreateNoteFolderUseCase
import com.mhss.app.domain.use_case.GetAllNoteFoldersUseCase
import com.mhss.app.domain.use_case.GetAllNotesUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.error_empty_title
import com.mhss.app.ui.errors.toMessageResId
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.toNotesView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class NotesViewModel(
    private val getAllNotes: GetAllNotesUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val createFolder: CreateNoteFolderUseCase,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is NoteException) {
            viewModelScope.launch {
                notesUiState.value.snackbarHostState.showSnackbar(throwable.toMessageResId())
            }
        }
    }

    private val _notesUiState = MutableStateFlow(UiState())
    val notesUiState = _notesUiState.asStateFlow()

    private val pagingRequest = MutableStateFlow<Pair<SortOrder, Boolean>>(SortOrder.DateModified(SortType.DESC) to false)
    val notes = pagingRequest.flatMapLatest { getAllNotes.paged(it.first, it.second) }.cachedIn(viewModelScope)

    private val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery.flatMapLatest {
        delay(250)
        searchNotes.paged(it)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            launch {
                combine(
                    getPreference(
                        intPreferencesKey(PrefsConstants.NOTES_ORDER_KEY),
                        SortOrder.DateModified(SortType.DESC).toInt()
                    ),
                    getPreference(
                        intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                        ItemView.LIST.value
                    ),
                    getPreference(
                        booleanPreferencesKey(PrefsConstants.SHOW_ALL_NOTES_KEY),
                        false
                    )
                ) { order, view, showAllNotes ->
                    val nextOrder = order.toOrder()
                    getNotes(nextOrder, showAllNotes)
                    _notesUiState.update {
                        it.copy(
                            notesSortOrder = nextOrder,
                            showAllNotes = showAllNotes,
                            noteView = view.toNotesView()
                        )
                    }
                }.collect()
            }

            launch {
                getAllFolders().collect { folders ->
                    _notesUiState.update {
                        it.copy(
                            folders = folders
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: NoteEvent) {
        when (event) {

            is NoteEvent.SearchNotes -> searchQuery.value = event.query

            is NoteEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.NOTES_ORDER_KEY),
                    event.sortOrder.toInt()
                )
            }

            is NoteEvent.UpdateView -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                    event.view.value
                )
            }

            is NoteEvent.ShowAllNotes -> viewModelScope.launch {
                savePreference(
                    booleanPreferencesKey(PrefsConstants.SHOW_ALL_NOTES_KEY),
                    event.showAll
                )
            }

            is NoteEvent.CreateFolder -> viewModelScope.launch(exceptionHandler) {
                if (event.name.isBlank()) {
                    notesUiState.value.snackbarHostState.showSnackbar(Res.string.error_empty_title)
                } else {
                    createFolder(event.name)
                }
            }

        }
    }

    data class UiState(
        val notesSortOrder: SortOrder = SortOrder.DateModified(SortType.DESC),
        val noteView: ItemView = ItemView.LIST,
        val navigateUp: Boolean = false,
        val folders: List<NoteFolder> = emptyList(),
        val showAllNotes: Boolean = false,
        val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    )

    private fun getNotes(sortOrder: SortOrder, showAllNotes: Boolean) {
        pagingRequest.value = sortOrder to showAllNotes
    }
}
