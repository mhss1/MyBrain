package com.mhss.app.domain.model

data class AiMessage(
    val content: String,
    val type: AiMessageType,
    val time: Long,
    val attachments: List<AiMessageAttachment> = emptyList(),
    val attachmentsText: String = ""
)

sealed interface AiMessageAttachment {
    data class Note (val note: com.mhss.app.domain.model.Note): AiMessageAttachment
    data class Task (val task: com.mhss.app.domain.model.Task): AiMessageAttachment
    data object CalenderEvents: AiMessageAttachment
}

enum class AiMessageType {
    USER,
    MODEL
}
