package com.mhss.app.domain.model

data class AiMessage(
    val message: String,
    val type: AiMessageType,
    val time: Long
)

enum class AiMessageType {
    USER,
    MODEL
}
