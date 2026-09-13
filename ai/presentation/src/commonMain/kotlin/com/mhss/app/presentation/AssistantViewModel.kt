@file:OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)

package com.mhss.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mhss.app.datetime.now
import com.mhss.app.datetime.todayPlusDays
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiRepositoryException
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.domain.use_case.CalendarEventsDay
import com.mhss.app.domain.use_case.DeleteAllAssistantThreadsUseCase
import com.mhss.app.domain.use_case.DeleteAssistantMessageUseCase
import com.mhss.app.domain.use_case.DeleteAssistantThreadUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetAssistantThreadsUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.GetThreadMessagesUseCase
import com.mhss.app.domain.use_case.SaveAssistantMessageUseCase
import com.mhss.app.domain.use_case.SaveAssistantThreadUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.SendAiMessageUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.model.toAiProvider
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.toIntList
import com.mhss.app.ui.toNotesView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KoinViewModel
class AssistantViewModel(
    private val sendAiMessage: SendAiMessageUseCase,
    private val getAssistantThreads: GetAssistantThreadsUseCase,
    private val saveAssistantMessage: SaveAssistantMessageUseCase,
    private val getThreadMessages: GetThreadMessagesUseCase,
    private val deleteAssistantThread: DeleteAssistantThreadUseCase,
    private val deleteAllAssistantThreads: DeleteAllAssistantThreadsUseCase,
    private val saveAssistantThread: SaveAssistantThreadUseCase,
    private val deleteAssistantMessage: DeleteAssistantMessageUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val searchTasks: SearchTasksUseCase,
    private val getCalendarEvents: GetAllEventsUseCase,
    private val getNoteById: GetNoteUseCase,
    private val getTaskById: GetTaskByIdUseCase,
) : ViewModel() {

    private val _currentThreadId = MutableStateFlow<String?>(null)
    val currentThreadId: StateFlow<String?> = _currentThreadId.asStateFlow()

    val messages: StateFlow<List<AiMessage>> = _currentThreadId
        .flatMapLatest { id ->
            id?.let { getThreadMessages(it) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val threads: StateFlow<List<AssistantThread>> = getAssistantThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        val error = if (e is AiRepositoryException) e.failure
        else AssistantResult.OtherError(e.message)
        _uiState.update { it.copy(error = error) }
    }

    private val noteSearchQuery = MutableStateFlow("")
    val noteSearchResults = noteSearchQuery.flatMapLatest { query ->
        delay(300)
        searchNotes.paged(query)
    }.cachedIn(viewModelScope)

    private val taskSearchQuery = MutableStateFlow("")
    val taskSearchResults = taskSearchQuery.flatMapLatest { query ->
        delay(300)
        searchTasks.paged(query)
    }.cachedIn(viewModelScope)

    private var sendMessageJob: Job? = null

    init {
        viewModelScope.launch(exceptionHandler) {
            getPreference(
                intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                ItemView.LIST.value
            ).onEach { value ->
                _uiState.update { it.copy(noteView = value.toNotesView()) }
            }.collect()
        }
        viewModelScope.launch(exceptionHandler) {
            getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id)
                .map { it.toAiProvider() }
                .collect { provider ->
                    _uiState.update { it.copy(aiEnabled = provider != AiProvider.None) }
                }
        }
    }

    fun onEvent(event: AssistantEvent) {
        when (event) {
            is AssistantEvent.SendMessage -> {
                sendMessageJob?.cancel()
                sendMessageJob = viewModelScope.launch(exceptionHandler) {
                    val currentThread = _currentThreadId.value
                    val threadId = currentThread ?: Uuid.generateV7().toString()

                    val title = event.content.take(100)
                    if (currentThread == null) {
                        val newThread = AssistantThread(
                            id = threadId,
                            title = title,
                            createdAt = now(),
                            updatedAt = now()
                        )
                        saveAssistantThread(newThread)
                        _currentThreadId.value = threadId
                    }

                    val userMessage = AiMessage.UserMessage(
                        content = event.content,
                        attachments = event.attachments,
                        attachmentsText = getAttachmentText(event.attachments),
                        time = now(),
                        uuid = Uuid.generateV7().toString()
                    )

                    val currentHistory = messages.value
                    saveAssistantMessage(threadId, userMessage)

                    _uiState.update {
                        it.copy(
                            attachments = emptyList(),
                            loading = true,
                            error = null
                        )
                    }

                    val allMessages = listOf(userMessage) + currentHistory

                    sendAiMessage(allMessages.asReversed())
                        .catch { e ->
                            delay(300)

                            val error = if (e is AiRepositoryException) {
                                e.failure
                            } else {
                                AssistantResult.OtherError(e.message)
                            }

                            val latestMessage = messages.value.firstOrNull()
                            if (latestMessage is AiMessage.UserMessage && error !is AssistantResult.ToolCallLimitExceeded) {
                                deleteAssistantMessage(latestMessage.uuid)
                            }

                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    error = error
                                )
                            }
                        }
                        .onCompletion {
                            _uiState.update { it.copy(loading = false) }
                        }
                        .collect { msg ->
                            saveAssistantMessage(threadId, msg)
                        }
                }
            }

            is AssistantEvent.SearchNotes -> {
                noteSearchQuery.value = event.query
            }

            is AssistantEvent.SearchTasks -> {
                taskSearchQuery.value = event.query
            }

            AssistantEvent.AddAttachmentEvents -> {
                _uiState.update {
                    it.copy(attachments = it.attachments + AiMessageAttachment.CalenderEvents)
                }
            }

            is AssistantEvent.AddAttachmentNote -> viewModelScope.launch {
                val note = getNoteById(event.id) ?: return@launch
                _uiState.update {
                    it.copy(
                        attachments = it.attachments + AiMessageAttachment.Note(
                            note.copy(
                                title = note.title.ifBlank { "Untitled Note" }
                            )
                        )
                    )
                }
            }

            is AssistantEvent.AddAttachmentTask -> viewModelScope.launch {
                val task = getTaskById(event.id) ?: return@launch
                _uiState.update {
                    it.copy(attachments = it.attachments + AiMessageAttachment.Task(task))
                }
            }

            is AssistantEvent.RemoveAttachment -> {
                _uiState.update { s ->
                    val list = s.attachments
                    if (event.index in list.indices) {
                        s.copy(attachments = list.filterIndexed { i, _ -> i != event.index })
                    } else {
                        s
                    }
                }
            }

            AssistantEvent.CancelMessage -> {
                sendMessageJob?.cancel()
                viewModelScope.launch(exceptionHandler) {
                    val latestMessage = messages.value.firstOrNull()
                    if (latestMessage is AiMessage.UserMessage) {
                        deleteAssistantMessage(latestMessage.uuid)
                    }
                }
                _uiState.update { it.copy(loading = false) }
            }

            AssistantEvent.NewChat -> {
                sendMessageJob?.cancel()
                _currentThreadId.value = null
                _uiState.update { it.copy(loading = false, error = null) }
            }

            is AssistantEvent.LoadThread -> {
                sendMessageJob?.cancel()
                _currentThreadId.value = event.threadId
                _uiState.update { it.copy(loading = false, error = null) }
            }

            is AssistantEvent.DeleteThread -> {
                viewModelScope.launch(exceptionHandler) {
                    if (_currentThreadId.value == event.threadId) {
                        sendMessageJob?.cancel()
                        _currentThreadId.value = null
                    }
                    deleteAssistantThread(event.threadId)
                }
            }

            AssistantEvent.DeleteAllThreads -> {
                viewModelScope.launch(exceptionHandler) {
                    sendMessageJob?.cancel()
                    _currentThreadId.value = null
                    deleteAllAssistantThreads()
                }
            }
        }
    }

    private suspend fun getAttachmentText(attachments: List<AiMessageAttachment>): String =
        withContext(Dispatchers.Default) {
            val builder = StringBuilder()
            if (attachments.isEmpty()) return@withContext ""
            builder.appendLine()
            builder.appendLine("Attached content from the user:")
            for (attachment in attachments) {
                when (attachment) {
                    is AiMessageAttachment.Note -> {
                        builder.appendLine("Attached Note:")
                        builder.appendLine(Json.encodeToString(attachment.note))
                    }

                    is AiMessageAttachment.Task -> {
                        builder.appendLine("Attached Task:")
                        builder.appendLine(Json.encodeToString(attachment.task))
                    }

                    is AiMessageAttachment.CalenderEvents -> {
                        builder.appendLine("Next 7 days events:")
                        builder.appendLine(Json.encodeToString(getEventsForNext7Days()))
                    }
                }
            }
            return@withContext builder.toString()
        }

    private suspend fun getEventsForNext7Days():  List<CalendarEventsDay> {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        return getCalendarEvents(excluded.toIntList(), todayPlusDays(7)).eventDays
    }


    data class UiState(
        val loading: Boolean = false,
        val error: AssistantResult.Failure? = null,
        val aiEnabled: Boolean = false,
        val noteView: ItemView = ItemView.LIST,
        val attachments: List<AiMessageAttachment> = emptyList(),
    )
}
