package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.ui.R
import com.mhss.app.domain.model.*
import com.mhss.app.domain.use_case.*
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.toNotesView
import com.mhss.app.util.date.now
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class NotesViewModel(
    private val folderlessNotes: GetAllFolderlessNotesUseCase,
    private val addNote: AddNoteUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val createFolder: AddNoteFolderUseCase,
    private val deleteFolder: DeleteNoteFolderUseCase,
    private val updateFolder: UpdateNoteFolderUseCase,
    private val getFolderNotes: GetNotesByFolderUseCase,
    private val getNoteFolder: GetNoteFolderUseCase,
) : ViewModel() {

    var notesUiState by mutableStateOf((UiState()))
        private set

    private var getNotesJob: Job? = null
    private var getFolderNotesJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                getPreference(
                    intPreferencesKey(PrefsConstants.NOTES_ORDER_KEY),
                    Order.DateModified(OrderType.ASC).toInt()
                ),
                getPreference(
                    intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                    ItemView.LIST.value
                ),
                getAllFolders()
            ) { order, view, folders ->
                notesUiState = notesUiState.copy(notesOrder = order.toOrder(), folders = folders)
                getFolderlessNotes(order.toOrder())
                if (notesUiState.noteView.value != view) {
                    notesUiState = notesUiState.copy(noteView = view.toNotesView())
                }
            }.collect()
        }
    }

    fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.AddNote -> viewModelScope.launch {
                if (event.note.title.isNotBlank() || event.note.content.isNotBlank()) {
                    addNote(
                        event.note.copy(
                            createdDate = now(),
                            updatedDate = now()
                        )
                    )
                }
            }

            is NoteEvent.SearchNotes -> viewModelScope.launch {
                notesUiState = notesUiState.copy(searchNotes = searchNotes(event.query))
            }

            is NoteEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.NOTES_ORDER_KEY),
                    event.order.toInt()
                )
            }

            is NoteEvent.ErrorDisplayed -> {
                notesUiState = notesUiState.copy(error = null)
            }

            is NoteEvent.UpdateView -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                    event.view.value
                )
            }

            is NoteEvent.CreateFolder -> viewModelScope.launch {
                if (event.folder.name.isBlank()) {
                    notesUiState = notesUiState.copy(error = R.string.error_empty_title)
                } else {
                    if (!notesUiState.folders.contains(event.folder)) {
                        createFolder(event.folder)
                    } else {
                        notesUiState = notesUiState.copy(error = R.string.error_folder_exists)
                    }
                }
            }

            is NoteEvent.DeleteFolder -> viewModelScope.launch {
                deleteFolder(event.folder)
                notesUiState = notesUiState.copy(navigateUp = true)
            }

            is NoteEvent.UpdateFolder -> viewModelScope.launch {
                notesUiState = if (event.folder.name.isBlank()) {
                    notesUiState.copy(error = R.string.error_empty_title)
                } else {
                    if (!notesUiState.folders.contains(event.folder)) {
                        updateFolder(event.folder)
                        notesUiState.copy(folder = event.folder)
                    } else {
                        notesUiState.copy(error = R.string.error_folder_exists)
                    }
                }
            }

            is NoteEvent.GetFolderNotes -> {
                getNotesFromFolder(event.id, notesUiState.notesOrder)
            }

            is NoteEvent.GetFolder -> viewModelScope.launch {
                val folder = getNoteFolder(event.id)
                notesUiState = notesUiState.copy(folder = folder)
            }
        }
    }

    data class UiState(
        val notes: List<Note> = emptyList(),
        val notesOrder: Order = Order.DateModified(OrderType.ASC),
        val error: Int? = null,
        val noteView: ItemView = ItemView.LIST,
        val navigateUp: Boolean = false,
        val searchNotes: List<Note> = emptyList(),
        val folders: List<NoteFolder> = emptyList(),
        val folderNotes: List<Note> = emptyList(),
        val folder: NoteFolder? = null
    )

    private fun getFolderlessNotes(order: Order) {
        getNotesJob?.cancel()
        getNotesJob = folderlessNotes(order)
            .onEach { notes ->
                notesUiState = notesUiState.copy(
                    notes = notes,
                    notesOrder = order
                )
            }.launchIn(viewModelScope)
    }

    private fun getNotesFromFolder(id: Int, order: Order) {
        getFolderNotesJob?.cancel()
        getFolderNotesJob = getFolderNotes(id, order)
            .onEach { notes ->
                val noteFolder = getNoteFolder(id)
                notesUiState = notesUiState.copy(
                    folderNotes = notes,
                    folder = noteFolder
                )
            }.launchIn(viewModelScope)
    }
}