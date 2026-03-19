package com.mhss.app.data.repository

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.InternalAgentToolsApi
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import com.mhss.app.data.EmptyAiClient
import com.mhss.app.data.buildChatPrompt
import com.mhss.app.data.buildChatSystemMessage
import com.mhss.app.data.getRootCause
import com.mhss.app.data.nowMillis
import com.mhss.app.data.toAiMessage
import com.mhss.app.data.toLLModel
import com.mhss.app.data.toNewAssistantMessage
import com.mhss.app.data.tools.BookmarkToolSet
import com.mhss.app.data.tools.CREATE_EVENTS_TOOL
import com.mhss.app.data.tools.CREATE_EVENT_TOOL
import com.mhss.app.data.tools.CREATE_MULTIPLE_NOTES_TOOL
import com.mhss.app.data.tools.CREATE_MULTIPLE_TASKS_TOOL
import com.mhss.app.data.tools.CREATE_NOTE_TOOL
import com.mhss.app.data.tools.CREATE_TASK_TOOL
import com.mhss.app.data.tools.CalendarEventIdResult
import com.mhss.app.data.tools.CalendarEventIdsResult
import com.mhss.app.data.tools.CalendarToolSet
import com.mhss.app.data.tools.DiaryToolSet
import com.mhss.app.data.tools.NoteIdResult
import com.mhss.app.data.tools.NoteIdsResult
import com.mhss.app.data.tools.NoteToolSet
import com.mhss.app.data.tools.SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL
import com.mhss.app.data.tools.SEARCH_NOTES_TOOL
import com.mhss.app.data.tools.SearchEventsResult
import com.mhss.app.data.tools.SearchNotesResult
import com.mhss.app.data.tools.TaskIdResult
import com.mhss.app.data.tools.TaskIdsResult
import com.mhss.app.data.tools.TaskToolSet
import com.mhss.app.data.tools.UtilToolSet
import com.mhss.app.domain.MAX_CONSECUTIVE_TOOL_CALLS
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiRepositoryException
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.domain.model.ToolCallResultObject
import com.mhss.app.domain.repository.AiRepository
import com.mhss.app.domain.use_case.GetCalendarEventByIdUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.preferences.PrefsConstants.AI_PROVIDER_KEY
import com.mhss.app.preferences.PrefsConstants.AI_TOOLS_ENABLED_KEY
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.model.toAiProvider
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.Uuid

@Factory
class AiRepositoryImpl(
    private val getPreferenceUseCase: GetPreferenceUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    private val noteToolSet: NoteToolSet,
    private val taskToolSet: TaskToolSet,
    private val calendarToolSet: CalendarToolSet,
    private val diaryToolSet: DiaryToolSet,
    private val bookmarkToolSet: BookmarkToolSet,
    private val utilToolSet: UtilToolSet,
    private val getNote: GetNoteUseCase,
    private val getTaskById: GetTaskByIdUseCase,
    private val getCalendarEventById: GetCalendarEventByIdUseCase
) : AiRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val toolRegistry = ToolRegistry {
        tools(noteToolSet)
        tools(taskToolSet)
        tools(calendarToolSet)
        tools(diaryToolSet)
        tools(bookmarkToolSet)
        tools(utilToolSet)
    }
    private val toolDescriptors = toolRegistry.tools.map { it.descriptor }
    private var chatSystemMessage = ""
    private var llmExecutor: PromptExecutor? = null
    private var llModel: LLModel? = null
    private var toolsEnabled: Boolean = false

    init {
        applicationScope.launch {
            val aiProvider = getPreferenceUseCase(
                intPreferencesKey(AI_PROVIDER_KEY),
                AiProvider.None.id
            ).first().toAiProvider()

            val toolsEnabledPreferenceValue = getPreferenceUseCase(
                booleanPreferencesKey(AI_TOOLS_ENABLED_KEY),
                false
            ).first()

            toolsEnabled = toolsEnabledPreferenceValue

            if (aiProvider == AiProvider.None) {
                llmExecutor = null
                llModel = null
                chatSystemMessage = ""
                return@launch
            }

            val key = getPreferenceUseCase(
                stringPreferencesKey(aiProvider.keyPref ?: "none"),
                ""
            ).first()

            val customUrlPref = aiProvider.customUrlPref
            val customUrl = if (aiProvider.supportsCustomUrl && customUrlPref != null) {
                getPreferenceUseCase(
                    stringPreferencesKey(customUrlPref),
                    ""
                ).first()
            } else {
                ""
            }

            val model = getPreferenceUseCase(
                stringPreferencesKey(aiProvider.modelPref ?: ""),
                ""
            ).first()

            if (model.isNotBlank()) {
                llModel = model.toLLModel(aiProvider, withTools = toolsEnabledPreferenceValue)
            }

            llModel?.let {
                llmExecutor = aiProvider.getExecutor(key, customUrl, it)
            }

            chatSystemMessage = buildChatSystemMessage(toolsEnabledPreferenceValue)
        }
    }

    override suspend fun sendPrompt(prompt: String): AssistantResult<String> {
        val client = llmExecutor ?: return AssistantResult.OtherError()
        val model = llModel ?: return AssistantResult.OtherError()

        val llmPrompt = prompt("user_prompt", LLMParams()) {
            user(prompt)
        }

        return try {
            val result = client.execute(prompt = llmPrompt, model = model)
            AssistantResult.Success(result.first().content)
        } catch (e: LLMClientException) {
            AssistantResult.OtherError(e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            AssistantResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            AssistantResult.OtherError(e.getRootCause().message ?: e.message)
        }
    }

    @OptIn(InternalAgentToolsApi::class)
    override fun sendMessage(messages: List<AiMessage>): Flow<AiMessage> = flow {
        val model =
            llModel ?: throw AiRepositoryException(AssistantResult.OtherError("Model not selected"))
        val executor = llmExecutor
            ?: throw AiRepositoryException(AssistantResult.OtherError("AI Client not initialized"))

        var currentMessages = messages
        var consecutiveToolCalls = 0

        try {
            do {
                if (consecutiveToolCalls >= MAX_CONSECUTIVE_TOOL_CALLS) {
                    throw AiRepositoryException(AssistantResult.ToolCallLimitExceeded)
                }

                val result = executor.execute(
                    prompt = currentMessages.buildChatPrompt(chatSystemMessage),
                    model = model,
                    tools = if (toolsEnabled) toolDescriptors else emptyList()
                )

                val toolCalls = result.filterIsInstance<Message.Tool.Call>()
                val assistantMessage =
                    result.filterIsInstance<Message.Assistant>().firstOrNull()
                        ?.toNewAssistantMessage()

                if (toolCalls.isEmpty()) {
                    assistantMessage?.let { emit(it) }
                    break
                }

                consecutiveToolCalls++

                val thoughtSignatures = extractThoughtSignatures(result)
                val toolCallMessages = toolCalls.map { toolCall ->
                    val toolCallMessageResult = executeToolCall(toolCall)
                    toolCall.toAiMessage(toolCallMessageResult, thoughtSignatures[toolCall]).also {
                        emit(it)
                    }
                }

                currentMessages = currentMessages + toolCallMessages

                if (assistantMessage != null) {
                    emit(assistantMessage)
                    currentMessages = currentMessages + assistantMessage
                }

            } while (toolCalls.isNotEmpty())
        } catch (e: AiRepositoryException) {
            throw e
        } catch (e: LLMClientException) {
            throw AiRepositoryException(AssistantResult.OtherError(e.message))
        } catch (e: IOException) {
            e.printStackTrace()
            throw AiRepositoryException(AssistantResult.InternetError)
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.getRootCause().message ?: e.message
            throw AiRepositoryException(AssistantResult.OtherError(message))
        }
    }

    private fun extractThoughtSignatures(
        result: List<Message.Response>
    ): Map<Message.Tool.Call, String?> {
        val signatures = HashMap<Message.Tool.Call, String?>()
        var lastSignature: String? = null
        for (msg in result) {
            when (msg) {
                is Message.Reasoning -> lastSignature = msg.encrypted
                is Message.Tool.Call -> {
                    signatures[msg] = lastSignature
                    lastSignature = null
                }
                is Message.Assistant -> lastSignature = null
            }
        }
        return signatures
    }

    private suspend fun executeToolCall(
        toolCall: Message.Tool.Call
    ): Result<AiMessage.ToolCall> = runCatching {
        val tool = toolRegistry.getTool(toolCall.tool)
        val args = tool.decodeArgs(toolCall.contentJson)
        val toolResult = (tool as Tool<Any?, Any?>).execute(args)
        val resultJson = tool.encodeResult(toolResult).toString()
        val resultObject = extractResultObject(tool.name, resultJson)
        AiMessage.ToolCall(
            uuid = Uuid.random().toString(),
            id = toolCall.id,
            name = tool.name,
            rawContent = toolCall.content,
            resultRawContent = resultJson,
            time = nowMillis(),
            resultObject = resultObject
        )
    }

    private suspend fun extractResultObject(
        toolName: String,
        resultJson: String
    ): ToolCallResultObject? = when (toolName) {
        SEARCH_NOTES_TOOL -> runCatching {
            val searchResult = json.decodeFromString<SearchNotesResult>(resultJson)
            if (searchResult.notes.size == 1) ToolCallResultObject.Notes(searchResult.notes) else null
        }.getOrNull()

        CREATE_NOTE_TOOL -> runCatching {
            val createResult = json.decodeFromString<NoteIdResult>(resultJson)
            getNote(createResult.createdNoteId)?.let {
                ToolCallResultObject.Notes(listOf(it))
            }
        }.getOrNull()

        CREATE_MULTIPLE_NOTES_TOOL -> runCatching {
            val createResult = json.decodeFromString<NoteIdsResult>(resultJson)
            val notes = createResult.createdNoteIds.mapNotNull { getNote(it) }
            if (notes.isNotEmpty()) ToolCallResultObject.Notes(notes) else null
        }.getOrNull()

        CREATE_TASK_TOOL -> runCatching {
            val createResult = json.decodeFromString<TaskIdResult>(resultJson)
            getTaskById(createResult.createdTaskId)?.let {
                ToolCallResultObject.Tasks(listOf(it))
            }
        }.getOrNull()

        CREATE_MULTIPLE_TASKS_TOOL -> runCatching {
            val createResult = json.decodeFromString<TaskIdsResult>(resultJson)
            val tasks = createResult.createdTaskIds.mapNotNull { getTaskById(it) }
            if (tasks.isNotEmpty()) ToolCallResultObject.Tasks(tasks) else null
        }.getOrNull()

        CREATE_EVENT_TOOL -> runCatching {
            val createResult = json.decodeFromString<CalendarEventIdResult>(resultJson)
            createResult.createdEventId?.let { id ->
                getCalendarEventById(id)?.let {
                    ToolCallResultObject.CalendarEvents(listOf(it))
                }
            }
        }.getOrNull()

        CREATE_EVENTS_TOOL -> runCatching {
            val createResult = json.decodeFromString<CalendarEventIdsResult>(resultJson)
            val events = createResult.createdEventIds.mapNotNull { id ->
                id?.let { getCalendarEventById(it) }
            }
            if (events.isNotEmpty()) ToolCallResultObject.CalendarEvents(events) else null
        }.getOrNull()

        SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL -> runCatching {
            val searchResult = json.decodeFromString<SearchEventsResult>(resultJson)
            if (searchResult.events.size == 1) ToolCallResultObject.CalendarEvents(searchResult.events) else null
        }.getOrNull()

        else -> null
    }

}

private fun AiProvider.getExecutor(key: String, customUrl: String, llModel: LLModel): PromptExecutor {
    val client = when (this) {
        AiProvider.OpenAI -> OpenAILLMClient(
            apiKey = key,
            settings = if (customUrl.isBlank()) OpenAIClientSettings() else OpenAIClientSettings(baseUrl = customUrl)
        )
        AiProvider.Gemini -> GoogleLLMClient(apiKey = key)
        AiProvider.Anthropic -> AnthropicLLMClient(
            apiKey = key,
            settings = AnthropicClientSettings(
                modelVersionsMap = mapOf(llModel to llModel.id)
            )
        )
        AiProvider.OpenRouter -> OpenRouterLLMClient(apiKey = key)
        AiProvider.Ollama -> if (customUrl.isBlank()) OllamaClient() else OllamaClient(customUrl)
        AiProvider.LmStudio -> OpenAILLMClient(
            apiKey = "",
            settings = OpenAIClientSettings(baseUrl = customUrl)
        )
        AiProvider.None -> EmptyAiClient
    }
    return SingleLLMPromptExecutor(client)
}