package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.app.R
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.autoFormatNotePrompt
import com.mhss.app.domain.correctSpellingNotePrompt
import com.mhss.app.domain.model.*
import com.mhss.app.domain.summarizeNotePrompt
import com.mhss.app.domain.use_case.*
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
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
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val createFolder: AddNoteFolderUseCase,
    private val deleteFolder: DeleteNoteFolderUseCase,
    private val updateFolder: UpdateNoteFolderUseCase,
    private val getFolderNotes: GetNotesByFolderUseCase,
    private val getNoteFolder: GetNoteFolderUseCase,
    private val sendAiPrompt: SendAiPromptUseCase
) : ViewModel() {

    var notesUiState by mutableStateOf((UiState()))
        private set

    private var getNotesJob: Job? = null
    private var getFolderNotesJob: Job? = null

    private lateinit var aiKey: String
    private lateinit var aiModel: String
    private lateinit var openaiURL: String
    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled
    var aiState by mutableStateOf((AiState()))
        private set

    private val aiProvider =
        getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id)
            .map { id -> AiProvider.entries.first { it.id == id } }
            .onEach { provider ->
                _aiEnabled.update { provider != AiProvider.None }
                when (provider) {
                    AiProvider.OpenAI -> {
                        aiKey = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                            ""
                        ).first()
                        aiModel = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                            AiConstants.OPENAI_DEFAULT_MODEL
                        ).first()
                        openaiURL = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_URL_KEY),
                            AiConstants.OPENAI_BASE_URL
                        ).first()
                    }

                    AiProvider.Gemini -> {
                        aiKey = getPreference(
                            stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                            ""
                        ).first()
                        aiModel = getPreference(
                            stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                            AiConstants.GEMINI_DEFAULT_MODEL
                        ).first()
                        openaiURL = ""
                    }

                    else -> {
                        aiKey = ""
                        aiModel = ""
                        openaiURL = ""
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, AiProvider.None)


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
                notesUiState = if (event.note.title.isBlank() && event.note.content.isBlank()) {
                    notesUiState.copy(navigateUp = true)
                } else {
                    addNote(
                        event.note.copy(
                            createdDate = now(),
                            updatedDate = now()
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
                val folder = getAllFolders().first().firstOrNull { it.id == note.folderId }
                notesUiState = notesUiState.copy(note = note, folder = folder, readingMode = true)
            }

            is NoteEvent.SearchNotes -> viewModelScope.launch {
                val notes = searchNotes(event.query)
                notesUiState = notesUiState.copy(searchNotes = notes)
            }

            is NoteEvent.UpdateNote -> viewModelScope.launch {
                notesUiState = if (event.note.title.isBlank() && event.note.content.isBlank())
                    notesUiState.copy(error = R.string.error_empty_note)
                else {
                    updateNote(event.note.copy(updatedDate = now()))
                    notesUiState.copy(navigateUp = true)
                }
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

            NoteEvent.ToggleReadingMode -> notesUiState =
                notesUiState.copy(readingMode = !notesUiState.readingMode)

            is NoteEvent.PinNote -> viewModelScope.launch {
                updateNote(notesUiState.note?.copy(pinned = !notesUiState.note?.pinned!!)!!)
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

            is AiAction -> viewModelScope.launch {
                val prompt = when (event) {
                    is NoteEvent.Summarize -> event.content.summarizeNotePrompt
                    is NoteEvent.AutoFormat -> event.content.autoFormatNotePrompt
                    is NoteEvent.CorrectSpelling -> event.content.correctSpellingNotePrompt
                }
                aiState = aiState.copy(
                    loading = true,
                    showAiSheet = true,
                    result = null,
                    error = null
                )
                val result = sendAiPrompt(prompt)
                aiState = when (result) {
                    is NetworkResult.Success<*> -> aiState.copy(
                        loading = false,
                        result = result.data as String,
                        error = null
                    )
                    is NetworkError -> aiState.copy(error = result, loading = false)
                }
            }
            NoteEvent.AiResultHandled -> aiState = aiState.copy(showAiSheet = false)
        }
    }

    private suspend fun sendAiPrompt(prompt: String): NetworkResult {
        return sendAiPrompt(
            prompt,
            aiKey,
            aiModel,
            aiProvider.value,
            openaiURL
        )
    }

    data class UiState(
        val notes: List<Note> = emptyList(),
        val note: Note? = null,
        val notesOrder: Order = Order.DateModified(OrderType.ASC),
        val error: Int? = null,
        val noteView: ItemView = ItemView.LIST,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false,
        val searchNotes: List<Note> = emptyList(),
        val folders: List<NoteFolder> = emptyList(),
        val folderNotes: List<Note> = emptyList(),
        val folder: NoteFolder? = null
    )

    data class AiState(
        val loading: Boolean = false,
        val result: String? = null,
        val error: NetworkError? = null,
        val showAiSheet: Boolean = false
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