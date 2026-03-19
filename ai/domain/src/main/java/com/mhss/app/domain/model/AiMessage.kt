package com.mhss.app.domain.model

sealed interface AiMessage {

    val uuid: String

    data class UserMessage(
        override val uuid: String,
        val content: String,
        val time: Long,
        val attachments: List<AiMessageAttachment> = emptyList(),
        val attachmentsText: String = ""
    ) : AiMessage

    data class AssistantMessage(val content: String, val time: Long, override val uuid: String) : AiMessage

    data class ToolCall(
        override val uuid: String,
        val id: String?,
        val name: String,
        val rawContent: String,
        val resultRawContent: String,
        val time: Long,
        val isFailed: Boolean = false,
        val resultObject: ToolCallResultObject? = null,
        val thoughtSignature: String? = null
    ): AiMessage
}

sealed interface ToolCallResultObject {
    data class Notes(val notes: List<Note>): ToolCallResultObject
    data class Tasks(val tasks: List<Task>): ToolCallResultObject
    data class CalendarEvents(val events: List<CalendarEvent>): ToolCallResultObject
}

sealed interface AiMessageAttachment {
    data class Note(val note: com.mhss.app.domain.model.Note) : AiMessageAttachment
    data class Task(val task: com.mhss.app.domain.model.Task) : AiMessageAttachment
    data object CalenderEvents : AiMessageAttachment
}
