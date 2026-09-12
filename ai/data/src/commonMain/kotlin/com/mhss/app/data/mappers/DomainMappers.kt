package com.mhss.app.data.mappers

import com.mhss.app.database.entity.AssistantAttachmentDto
import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantMessageMetadata
import com.mhss.app.database.entity.ToolPreview
import com.mhss.app.database.entity.ToolCallMetadata
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.ToolCallResultObject

fun AiMessage.toAssistantMessageEntity(threadId: String, syncSeq: Long = 0L): AssistantMessageEntity {
        return when (this) {
            is AiMessage.UserMessage -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.USER.key,
                    content = content,
                    createdAt = time,
                    metadata = if (attachments.isNotEmpty()) AssistantMessageMetadata(
                        attachmentsText = attachmentsText,
                        attachments = attachments.map { it.toDto() }
                    ) else null,
                    syncSeq = syncSeq
                )
            }

            is AiMessage.AssistantMessage -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.ASSISTANT.key,
                    content = content,
                    createdAt = time,
                    syncSeq = syncSeq
                )
            }

            is AiMessage.ToolCall -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.TOOL_CALL.key,
                    content = "",
                    createdAt = time,
                    metadata = AssistantMessageMetadata(
                        toolCall = ToolCallMetadata(
                            id = id,
                            name = name,
                            rawContent = rawContent,
                            resultRawContent = resultRawContent,
                            isFailed = isFailed,
                            thoughtSignature = thoughtSignature
                        ),
                        toolPreviews = resultObject?.toToolPreviews()
                    ),
                    syncSeq = syncSeq
                )
            }
        }
    }

fun AssistantMessageEntity.toAiMessage(): AiMessage? =
    when (type) {
        AiMessageType.USER.key -> {
            AiMessage.UserMessage(
                uuid = id,
                content = content,
                attachmentsText = metadata?.attachmentsText.orEmpty(),
                attachments = metadata?.attachments?.map { it.toDomain() }.orEmpty(),
                time = createdAt
            )
        }

        AiMessageType.ASSISTANT.key -> {
            AiMessage.AssistantMessage(
                uuid = id,
                content = content,
                time = createdAt
            )
        }

        AiMessageType.TOOL_CALL.key -> {
            val tc = metadata?.toolCall ?: return null
            AiMessage.ToolCall(
                uuid = id,
                id = tc.id,
                name = tc.name,
                rawContent = tc.rawContent,
                resultRawContent = tc.resultRawContent,
                time = createdAt,
                isFailed = tc.isFailed,
                thoughtSignature = tc.thoughtSignature,
                resultObject = metadata?.toolPreviews.orEmpty().toResultObject()
            )
        }

        else -> null
    }


private fun AiMessageAttachment.toDto(): AssistantAttachmentDto = when (this) {
    is AiMessageAttachment.Note -> AssistantAttachmentDto.Note(note)
    is AiMessageAttachment.Task -> AssistantAttachmentDto.Task(task)
    AiMessageAttachment.CalenderEvents -> AssistantAttachmentDto.CalendarEvents
}

private fun AssistantAttachmentDto.toDomain(): AiMessageAttachment = when (this) {
    is AssistantAttachmentDto.Note -> AiMessageAttachment.Note(note)
    is AssistantAttachmentDto.Task -> AiMessageAttachment.Task(task)
    AssistantAttachmentDto.CalendarEvents -> AiMessageAttachment.CalenderEvents
}

private fun ToolCallResultObject.toToolPreviews(): List<ToolPreview> = when (this) {
    is ToolCallResultObject.Notes -> notes.take(MAX_TOOL_PREVIEWS).map {
        ToolPreview.Note(
            id = it.id,
            title = it.title,
            content = it.content.take(NOTE_PREVIEW_CONTENT_LIMIT + 1),
            updatedDate = it.updatedDate,
            folderId = it.folderId
        )
    }

    is ToolCallResultObject.Tasks -> tasks.take(MAX_TOOL_PREVIEWS).map {
        ToolPreview.Task(
            id = it.id,
            title = it.title,
            isCompleted = it.isCompleted,
            priority = it.priority.name,
            dueDate = it.dueDate ?: 0L,
            completedSubTasks = it.subTasks.count { subTask -> subTask.isCompleted },
            totalSubTasks = it.subTasks.size
        )
    }

    is ToolCallResultObject.CalendarEvents -> events.take(MAX_TOOL_PREVIEWS).map {
        ToolPreview.CalendarEvent(
            id = it.id,
            title = it.title,
            start = it.start,
            end = it.end,
            location = it.location,
            allDay = it.allDay,
            color = it.color,
            calendarId = it.calendarId
        )
    }
}

private fun List<ToolPreview>.toResultObject(): ToolCallResultObject? = when (firstOrNull()) {
    is ToolPreview.Note -> ToolCallResultObject.Notes(
        filterIsInstance<ToolPreview.Note>().map {
            Note(
                id = it.id,
                title = it.title,
                content = it.content,
                updatedDate = it.updatedDate,
                folderId = it.folderId
            )
        }
    )

    is ToolPreview.Task -> ToolCallResultObject.Tasks(
        filterIsInstance<ToolPreview.Task>().map {
            Task(
                id = it.id,
                title = it.title,
                isCompleted = it.isCompleted,
                priority = Priority.entries.firstOrNull { priority -> priority.name == it.priority }
                    ?: Priority.LOW,
                dueDate = it.dueDate.takeUnless { date -> date == 0L },
                subTasks = List(it.totalSubTasks) { index ->
                    SubTask(isCompleted = index < it.completedSubTasks)
                }
            )
        }
    )

    is ToolPreview.CalendarEvent -> ToolCallResultObject.CalendarEvents(
        filterIsInstance<ToolPreview.CalendarEvent>().map {
            CalendarEvent(
                id = it.id,
                title = it.title,
                start = it.start,
                end = it.end,
                location = it.location,
                allDay = it.allDay,
                color = it.color,
                calendarId = it.calendarId
            )
        }
    )

    null -> null
}

private const val MAX_TOOL_PREVIEWS = 5
private const val NOTE_PREVIEW_CONTENT_LIMIT = 60
