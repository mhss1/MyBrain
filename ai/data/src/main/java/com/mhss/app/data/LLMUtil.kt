package com.mhss.app.data

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import com.mhss.app.domain.baseChatSystemMessage
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.toolsSystemMessage
import com.mhss.app.preferences.domain.model.AiProvider
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid


fun List<AiMessage>.buildChatPrompt(systemMessage: String) = prompt("chat_prompt", LLMParams()) {
    system(systemMessage)
    forEach { message ->
        when (message) {
            is AiMessage.UserMessage -> user(message.content + message.attachmentsText)
            is AiMessage.AssistantMessage -> assistant(message.content)
            is AiMessage.ToolCall -> tool {
                call(
                    id = message.id,
                    tool = message.name,
                    content = message.rawContent
                )
                result(
                    id = message.id,
                    tool = message.name,
                    content = message.resultRawContent
                )
            }
        }
    }
}


fun Message.Tool.Call.toAiMessage(toolCallResult: Result<AiMessage.ToolCall>): AiMessage {
    return toolCallResult.getOrNull()
        ?: AiMessage.ToolCall(
            uuid = Uuid.random().toString(),
            id = id,
            name = tool,
            rawContent = content,
            resultRawContent = toolCallResult.exceptionOrNull()
                ?.getRootCause()
                ?.toString()
                ?: "Error executing tool",
            time = nowMillis(),
            isFailed = true
        )
}

fun String.toLLModel(provider: AiProvider, withTools: Boolean): LLModel {
    val llmProvider = provider.toLLMProvider()
    return LLModel(
        provider = llmProvider,
        id = this,
        capabilities = buildList {
            // Koog internally throws if an Anthropic model doesn't declare tool capabilities
            if (withTools || provider == AiProvider.Anthropic) {
                add(LLMCapability.Tools)
                add(LLMCapability.ToolChoice)
            }
            add(LLMCapability.Completion)
            if (llmProvider == LLMProvider.OpenAI){
                add(LLMCapability.OpenAIEndpoint.Responses)
                add(LLMCapability.OpenAIEndpoint.Completions)
            }
        },
        contextLength = 128_000,
        maxOutputTokens = 32_000,
    )
}

fun AiProvider.toLLMProvider() = when (this) {
    AiProvider.OpenAI -> LLMProvider.OpenAI
    AiProvider.Gemini -> LLMProvider.Google
    AiProvider.Anthropic -> LLMProvider.Anthropic
    AiProvider.OpenRouter -> LLMProvider.OpenRouter
    AiProvider.Ollama -> LLMProvider.Ollama
    AiProvider.LmStudio -> LLMProvider.OpenAI
    AiProvider.None -> LLMProvider.OpenAI // just a placeholder
}

fun Message.Assistant.toNewAssistantMessage() = AiMessage.AssistantMessage(
    uuid = Uuid.random().toString(),
    content = content,
    time = nowMillis()
)

internal fun nowMillis() = Clock.System.now().toEpochMilliseconds()
private val currentTimeZone = TimeZone.currentSystemDefault()
internal fun currentLocalDateTime() = Clock.System.now().toLocalDateTime(currentTimeZone)
internal const val llmDateTimeFormatUnicode = "HH:mm dd-MM-yyyy"

internal val llmDateTimeWithDayNameFormat = LocalDateTime.Format {
    hour(); char(':'); minute();
    char(' ');
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    char(' ');
    day(); char('-'); monthNumber(); char('-'); year()
}
internal val llmDateTimeFormat = LocalDateTime.Format {
    byUnicodePattern(llmDateTimeFormatUnicode)
}
internal fun String.parseDateTimeFromLLM() = runCatching {
    LocalDateTime.parse(this, llmDateTimeFormat).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}.getOrNull()

fun buildChatSystemMessage(toolsEnabled: Boolean) = buildString {
    appendLine(baseChatSystemMessage)
    if (toolsEnabled) appendLine(toolsSystemMessage)
    append("Current date & time: ")
    appendLine(currentLocalDateTime().format(llmDateTimeWithDayNameFormat))
    append("Time zone: "); append(currentTimeZone)
}

fun Throwable.getRootCause(): Throwable {
    var rootCause: Throwable? = this
    while (rootCause?.cause != null) {
        rootCause = rootCause.cause
    }
    return rootCause ?: this
}