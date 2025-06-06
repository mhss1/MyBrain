package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.autoFormatNotePrompt
import com.mhss.app.domain.correctSpellingNotePrompt
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.summarizeNotePrompt
import com.mhss.app.domain.use_case.AddNoteUseCase
import com.mhss.app.domain.use_case.DeleteNoteUseCase
import com.mhss.app.domain.use_case.GetAllNoteFoldersUseCase
import com.mhss.app.domain.use_case.GetNoteFolderUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.SendAiPromptUseCase
import com.mhss.app.domain.use_case.UpdateNoteUseCase
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class NoteDetailsViewModel(
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val getNoteFolder: GetNoteFolderUseCase,
    private val sendAiPrompt: SendAiPromptUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    id: Int,
    folderId: Int,
) : ViewModel() {

    var noteUiState by mutableStateOf(UiState())
        private set

    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set

    private var autoSaveJob: Job? = null
    private val debounceTime = 2000L

    private lateinit var aiKey: String
    private lateinit var aiModel: String
    private lateinit var openaiURL: String
    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled
    var aiState by mutableStateOf((AiState()))
        private set
    private var aiActionJob: Job? = null

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
            val note: Note? = if (id != -1) getNote(id) else null
            val folder = getNoteFolder(note?.folderId ?: folderId)
            val folders = getAllFolders().first()

            if (note != null) {
                title = note.title
                content = note.content
            }

            noteUiState = noteUiState.copy(
                note = note,
                folder = folder,
                folders = folders,
                readingMode = note != null,
                pinned = note?.pinned ?: false
            )
        }
    }

    fun onEvent(event: NoteDetailsEvent) {
        when (event) {
            is NoteDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!noteUiState.navigateUp) {
                    autoSaveJob?.cancel()
                    saveNote()
                }
            }

            is NoteDetailsEvent.DeleteNote -> viewModelScope.launch {
                deleteNote(event.note)
                noteUiState = noteUiState.copy(navigateUp = true)
            }

            NoteDetailsEvent.ToggleReadingMode -> noteUiState =
                noteUiState.copy(readingMode = !noteUiState.readingMode)

            is NoteDetailsEvent.UpdateTitle -> {
                title = event.title
                saveNoteWithDebounce()
            }

            is NoteDetailsEvent.UpdateContent -> {
                content = event.content
                saveNoteWithDebounce()
            }

            is NoteDetailsEvent.UpdateFolder -> {
                noteUiState = noteUiState.copy(folder = event.folder)
                saveNoteWithDebounce()
            }

            is NoteDetailsEvent.UpdatePinned -> {
                noteUiState = noteUiState.copy(pinned = event.pinned)
                saveNoteWithDebounce()
            }

            is AiAction -> aiActionJob = viewModelScope.launch {
                val prompt = when (event) {
                    is NoteDetailsEvent.Summarize -> event.content.summarizeNotePrompt
                    is NoteDetailsEvent.AutoFormat -> event.content.autoFormatNotePrompt
                    is NoteDetailsEvent.CorrectSpelling -> event.content.correctSpellingNotePrompt
                }
                aiState = aiState.copy(
                    loading = true,
                    showAiSheet = true,
                    result = null,
                    error = null
                )
                val result = sendAiPrompt(prompt)
                aiState = when (result) {
                    is NetworkResult.Success -> aiState.copy(
                        loading = false,
                        result = result.data,
                        error = null
                    )

                    is NetworkResult.Failure -> aiState.copy(error = result, loading = false)
                }
            }

            NoteDetailsEvent.AiResultHandled -> {
                aiActionJob?.cancel()
                aiActionJob = null
                aiState = aiState.copy(showAiSheet = false)
            }
        }
    }

    private suspend fun sendAiPrompt(prompt: String) = sendAiPrompt(
        prompt,
        aiKey,
        aiModel,
        aiProvider.value,
        openaiURL
    )

    private fun saveNoteWithDebounce() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(debounceTime)
            saveNote()
        }
    }

    private suspend fun saveNote() {
        if (noteUiState.navigateUp) return

        val folderId = noteUiState.folder?.id
        val pinned = noteUiState.pinned

        if (noteUiState.note == null) {
            if (title.isNotBlank() || content.isNotBlank()) {
                val note = Note(
                    title = title,
                    content = content,
                    folderId = folderId,
                    pinned = pinned,
                    createdDate = now(),
                    updatedDate = now()
                )
                val id = addNote(note)
                noteUiState = noteUiState.copy(note = note.copy(id = id.toInt()))
            }
        } else {
            val currentNote = noteUiState.note!!
            if (currentNote.title != title ||
                currentNote.content != content ||
                currentNote.folderId != folderId ||
                currentNote.pinned != pinned
            ) {
                val newNote = currentNote.copy(
                    title = title,
                    content = content,
                    folderId = folderId,
                    pinned = pinned,
                    updatedDate = now()
                )
                updateNote(newNote)
                noteUiState = noteUiState.copy(note = newNote)
            }
        }
    }

    data class UiState(
        val note: Note? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false,
        val folders: List<NoteFolder> = emptyList(),
        val folder: NoteFolder? = null,
        val pinned: Boolean = false
    )

    data class AiState(
        val loading: Boolean = false,
        val result: String? = null,
        val error: NetworkResult.Failure? = null,
        val showAiSheet: Boolean = false,
    )
}