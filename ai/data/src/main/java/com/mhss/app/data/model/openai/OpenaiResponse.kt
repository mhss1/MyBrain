package com.mhss.app.data.model.openai

import com.mhss.app.data.NetworkConstants.OPENAI_MESSAGE_MODEL_TYPE
import com.mhss.app.data.NetworkConstants.OPENAI_MESSAGE_USER_TYPE
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class OpenaiResponse(
    val choices: List<OpenaiChoice>
)

@Serializable
data class OpenaiChoice(
    val message: OpenaiMessage
)

fun OpenaiMessage.toAiMessage() = AiMessage(
    message = content,
    type = if (role == OPENAI_MESSAGE_USER_TYPE) AiMessageType.USER else AiMessageType.MODEL,
    time = Clock.System.now().toEpochMilliseconds()
)

val AiMessageType.openaiRole
    get() = if (this == AiMessageType.USER) OPENAI_MESSAGE_USER_TYPE else OPENAI_MESSAGE_MODEL_TYPE