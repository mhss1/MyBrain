package com.mhss.app.data.model.gemini

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.systemMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiMessageRequestBody(
    val contents: List<GeminiMessage>,
    @SerialName("system_instruction")
    val systemInstruction: GeminiMessage? = null,
)

@Serializable
data class GeminiMessage(
    val parts: List<GeminiMessagePart>,
    val role: String = ""
)

@Serializable
data class GeminiMessagePart(
    val text: String
)

fun String.toGeminiRequestBody() = GeminiMessageRequestBody(
    contents = listOf(
        GeminiMessage(
            listOf(
                GeminiMessagePart(this)
            )
        )
    )
)

fun List<AiMessage>.toGeminiRequestBody() = GeminiMessageRequestBody(
    contents = map {
        GeminiMessage(
            parts = listOf(GeminiMessagePart(it.content + it.attachmentsText)),
            role = it.type.geminiRole
        )
    },
    systemInstruction = GeminiMessage(listOf(GeminiMessagePart(systemMessage)))
)