package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.SendAiMessageUseCase
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.todayPlusDays
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import java.util.LinkedList

@KoinViewModel
class AssistantViewModel(
    private val sendAiMessage: SendAiMessageUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val searchTasks: SearchTasksUseCase,
    private val getCalendarEvents: GetAllEventsUseCase
) : ViewModel() {

    private val messages = ArrayList<AiMessage>()
    // LinkedList to enable inserting at the beginning of the list efficiently
    // LazyColumn needs the list in reverse order
    private val uiMessages = LinkedList<AiMessage>()
    var uiState by mutableStateOf(UiState())
        private set

    private lateinit var aiKey: String
    private lateinit var aiModel: String
    private lateinit var openaiURL: String
    var aiEnabled by mutableStateOf(false)
        private set

    private val aiProvider =
        getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id)
            .map { id -> AiProvider.entries.first { it.id == id } }
            .onEach { provider ->
                aiEnabled = provider != AiProvider.None
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

    fun onEvent(event: AssistantEvent) {
        when (event) {
            is AssistantEvent.SendMessage -> viewModelScope.launch {
                val message = event.message.copy(
                    content = event.message.content,
                    // copy of the list
                    attachments = event.message.attachments,
                    attachmentsText = getAttachmentText(event.message.attachments)
                )
                messages.add(message)
                uiMessages.addFirst(message)

                uiState = uiState.copy(
                    messages = uiMessages,
                    loading = true,
                    error = null
                )
                val result = sendAiMessage(
                    messages,
                    aiKey,
                    aiModel,
                    aiProvider.value,
                    openaiURL
                )
                when (result) {
                    is NetworkResult.Success -> {
                        messages.add(result.data)
                        uiMessages.addFirst(result.data)

                        uiState = uiState.copy(messages = uiMessages, loading = false)
                    }

                    is NetworkResult.Failure -> {
                        messages.removeLast()
                        delay(300)
                        uiMessages.removeFirst()

                        uiState = uiState.copy(
                            messages = uiMessages,
                            loading = false,
                            error = result
                        )
                    }
                }
            }
        }
    }

    private suspend fun getAttachmentText(attachments: List<AiMessageAttachment>): String {
        val builder = StringBuilder()
        if (attachments.isEmpty()) return ""
        builder.appendLine()
        builder.appendLine("Attached content from the user:")
        for (attachment in attachments) {
            when (attachment) {
                is AiMessageAttachment.Note -> {
                    builder.appendLine("Attached Notes:")
                    builder.appendLine(Json.encodeToString(attachment.note))
                }

                is AiMessageAttachment.Task -> {
                    builder.appendLine("Attached Tasks:")
                    builder.appendLine(Json.encodeToString(attachment.task))
                }

                is AiMessageAttachment.CalenderEvents -> {
                    builder.appendLine("Next 7 days events:")
                    builder.appendLine(Json.encodeToString(getEventsForNext7Days()))
                }
            }
        }
        return builder.toString()
    }

    private suspend fun getEventsForNext7Days(): Map<String, List<CalendarEvent>> {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        return getCalendarEvents(excluded.toIntList(),  todayPlusDays(7)) {
            it.start.formatDateForMapping()
        }
    }


    data class UiState(
        val messages: List<AiMessage> = emptyList(),
        val loading: Boolean = false,
        val error: NetworkResult.Failure? = null
    )
}