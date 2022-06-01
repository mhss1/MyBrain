package com.mhss.app.mybrain.presentation.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.use_case.notes.*
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.SaveSettingsUseCase
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val allNotes: GetAllNotesUseCase,
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val createFolder: AddNoteFolderUseCass,
    private val deleteFolder: DeleteNoteFolderUseCass,
    private val updateFolder: UpdateNoteFolderUseCass,
    private val getFolderNotes: GetNotesByFolderUseCase
) : ViewModel() {

    var notesUiState by mutableStateOf((UiState()))
        private set

    private var getNotesJob: Job? = null
    private var getFolderNotesJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                getSettings(
                    intPreferencesKey(Constants.NOTES_ORDER_KEY),
                    Order.DateModified(OrderType.ASC()).toInt()
                ),
                getSettings(
                    intPreferencesKey(Constants.NOTE_VIEW_KEY),
                    ItemView.LIST.value
                ),
                getAllFolders()
            ) { order, view, folders ->
                notesUiState = notesUiState.copy(notesOrder = order.toOrder(), folders = folders)
                getNotes(order.toOrder())
                if (notesUiState.noteView.value != view) {
                    notesUiState = notesUiState.copy(noteView = view.toNotesView())
                }
            }.collect()
        }
    }

    fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.AddNote -> viewModelScope.launch {
                notesUiState = if (event.note.title.isBlank() && event.note.content.isBlank()) {
                    notesUiState.copy(navigateUp = true)
                } else {
                    addNote(
                        event.note.copy(
                            createdDate = System.currentTimeMillis(),
                            updatedDate = System.currentTimeMillis()
                        )
                    )
                    notesUiState.copy(navigateUp = true)
                }
            }
            is NoteEvent.DeleteNote -> viewModelScope.launch {
                deleteNote(event.note)
                notesUiState = notesUiState.copy(navigateUp = true)
            }
            is NoteEvent.GetNote -> viewModelScope.launch {
                val note = getNote(event.noteId)
                notesUiState = notesUiState.copy(note = note)
            }
            is NoteEvent.SearchNotes -> viewModelScope.launch {
                val notes = searchNotes(event.query)
                notesUiState = notesUiState.copy(searchNotes = notes)
            }
            is NoteEvent.UpdateNote -> viewModelScope.launch {
                notesUiState = if (event.note.title.isBlank() && event.note.content.isBlank())
                    notesUiState.copy(error = getString(R.string.error_empty_note))
                else {
                    updateNote(event.note.copy(updatedDate = System.currentTimeMillis()))
                    notesUiState.copy(navigateUp = true)
                }
            }
            is NoteEvent.UpdateOrder -> viewModelScope.launch {
                saveSettings(
                    intPreferencesKey(Constants.NOTES_ORDER_KEY),
                    event.order.toInt()
                )
            }
            is NoteEvent.ErrorDisplayed -> notesUiState = notesUiState.copy(error = null)
            NoteEvent.ToggleReadingMode -> notesUiState =
                notesUiState.copy(readingMode = !notesUiState.readingMode)
            is NoteEvent.PinNote -> viewModelScope.launch {
                updateNote(notesUiState.note?.copy(pinned = !notesUiState.note?.pinned!!)!!)
            }
            is NoteEvent.UpdateView -> viewModelScope.launch {
                saveSettings(
                    intPreferencesKey(Constants.NOTE_VIEW_KEY),
                    event.view.value
                )
            }
            is NoteEvent.CreateFolder -> viewModelScope.launch {
                if (event.folder.name.isBlank()) {
                    notesUiState = notesUiState.copy(error = getString(R.string.error_empty_title))
                } else {
                    if (!notesUiState.folders.contains(event.folder)) {
                        createFolder(event.folder)
                    } else {
                        notesUiState = notesUiState.copy(error = getString(R.string.error_folder_exists))
                    }
                }
            }
            is NoteEvent.DeleteFolder -> viewModelScope.launch {
                deleteFolder(event.folder)
            }
            is NoteEvent.UpdateFolder -> viewModelScope.launch {
                if (event.folder.name.isBlank()) {
                    notesUiState = notesUiState.copy(error = getString(R.string.error_empty_title))
                } else {
                    if (!notesUiState.folders.contains(event.folder)) {
                        updateFolder(event.folder)
                    } else {
                        notesUiState = notesUiState.copy(error = getString(R.string.error_folder_exists))
                    }
                }
            }
            is NoteEvent.GetFolderNotes -> {
                getNotesFromFolder(event.folderName, notesUiState.notesOrder)
            }
        }
    }

    data class UiState(
        val notes: List<Note> = emptyList(),
        val note: Note? = null,
        val notesOrder: Order = Order.DateModified(OrderType.ASC()),
        val error: String? = null,
        val noteView: ItemView = ItemView.LIST,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = true,
        val searchNotes: List<Note> = emptyList(),
        val folders: List<NoteFolder> = emptyList(),
        val folderNotes: List<Note> = emptyList()
    )

    private fun getNotes(order: Order) {
        getNotesJob?.cancel()
        getNotesJob = allNotes(order)
            .onEach { notes ->
                notesUiState = notesUiState.copy(
                    notes = notes,
                    notesOrder = order
                )
            }.launchIn(viewModelScope)
    }

    private fun getNotesFromFolder(folder: String, order: Order) {
        getFolderNotesJob?.cancel()
        getFolderNotesJob = getFolderNotes(folder, order)
            .onEach { notes ->
                notesUiState = notesUiState.copy(
                    folderNotes = notes
                )
            }.launchIn(viewModelScope)
    }
}