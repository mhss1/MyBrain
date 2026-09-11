package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteException
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.use_case.DeleteNoteFolderUseCase
import com.mhss.app.domain.use_case.GetNoteFolderUseCase
import com.mhss.app.domain.use_case.GetNotesByFolderUseCase
import com.mhss.app.domain.use_case.UpdateNoteFolderUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.error_empty_title
import com.mhss.app.ui.errors.toMessageResId
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.toNotesView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class NoteFolderDetailsViewModel(
    private val getFolderNotes: GetNotesByFolderUseCase,
    private val getNoteFolder: GetNoteFolderUseCase,
    private val updateFolder: UpdateNoteFolderUseCase,
    private val deleteFolder: DeleteNoteFolderUseCase,
    private val getPreference: GetPreferenceUseCase,
    @Provided id: String,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is NoteException) {
            viewModelScope.launch {
                _uiState.value.snackbarHostState.showSnackbar(throwable.toMessageResId())
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    var uiState = _uiState.asStateFlow()

    private val folderId = id
    private var getFolderNotesJob: Job? = null

    init {
        viewModelScope.launch {
            launch {
                _uiState.update {
                    it.copy(folder = getNoteFolder(folderId))
                }
            }
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
                ) { order, view ->
                    val nextOrder = order.toOrder()
                    _uiState.update {
                        it.copy(
                            notesSortOrder = nextOrder,
                            noteView = view.toNotesView(),
                        )
                    }
                    getNotesFromFolder(nextOrder)
                }.collect()
            }
        }
    }

    fun deleteCurrentFolder() {
        val folder = uiState.value.folder ?: return
        viewModelScope.launch {
            deleteFolder(folder)
            _uiState.update { it.copy(navigateUp = true) }
        }
    }

    fun updateCurrentFolderName(name: String) {
        val currentFolder = uiState.value.folder ?: return
        val trimmedName = name.trim()
        viewModelScope.launch(exceptionHandler) {
            if (trimmedName.isBlank()) {
                uiState.value.snackbarHostState.showSnackbar(Res.string.error_empty_title)
                return@launch
            }

            val updated = currentFolder.copy(name = trimmedName)
            updateFolder(updated)
            _uiState.update { it.copy(folder = updated) }
        }
    }

    private fun getNotesFromFolder(notesSortOrder: SortOrder) {
        getFolderNotesJob?.cancel()
        getFolderNotesJob = getFolderNotes(folderId, notesSortOrder)
            .onEach { notes ->
                _uiState.update {
                    it.copy(folderNotes = notes, notesSortOrder = notesSortOrder)
                }
            }
            .launchIn(viewModelScope)
    }

    data class UiState(
        val folder: NoteFolder? = null,
        val folderNotes: List<Note> = emptyList(),
        val noteView: ItemView = ItemView.LIST,
        val notesSortOrder: SortOrder = SortOrder.DateModified(SortType.DESC),
        val navigateUp: Boolean = false,
        val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    )
}