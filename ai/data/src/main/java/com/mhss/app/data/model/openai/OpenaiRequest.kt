package com.mhss.app.data.model.openai

import com.mhss.app.data.NetworkConstants
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.systemMessage
import kotlinx.serialization.Serializable

@Serializable
data class OpenaiMessageRequestBody(
    val model: String,
    val messages: List<OpenaiMessage>
)

@Serializable
data class OpenaiMessage(
    val content: String,
    val role: String
)

fun List<AiMessage>.toOpenAiRequestBody(
    model: String,
): OpenaiMessageRequestBody {
    return OpenaiMessageRequestBody(
        model = model,
        messages =
        listOf(
            OpenaiMessage(
                content = systemMessage,
                role = NetworkConstants.OPENAI_MESSAGE_SYSTEM_TYPE
            )
        ) + map {
            OpenaiMessage(
                content = it.content + it.attachmentsText,
                role = it.type.openaiRole
            )
        }
    )
}