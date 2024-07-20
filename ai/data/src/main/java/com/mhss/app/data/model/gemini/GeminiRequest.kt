package com.mhss.app.data.model.gemini

import com.mhss.app.domain.model.AiMessage
import kotlinx.serialization.Serializable

@Serializable
data class GeminiMessageRequestBody(
    val contents: List<GeminiMessage>
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
    listOf(
        GeminiMessage(
            listOf(
                GeminiMessagePart(this)
            )
        )
    )
)

fun List<AiMessage>.toGeminiRequestBody() = GeminiMessageRequestBody(
    map {
        GeminiMessage(
            parts = listOf(GeminiMessagePart(it.message)),
            role = it.type.geminiRole
        )
    }
)