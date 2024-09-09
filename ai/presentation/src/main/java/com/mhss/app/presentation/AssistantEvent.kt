package com.mhss.app.presentation

import com.mhss.app.domain.model.AiMessage

sealed interface AssistantEvent {
    data class SendMessage(val message: AiMessage): AssistantEvent
    data class SearchNotes(val query: String) : AssistantEvent
    data class SearchTasks(val query: String) : AssistantEvent
    data class AddAttachmentNote(val id: Int): AssistantEvent
    data class AddAttachmentTask(val id: Int): AssistantEvent
    data object AddAttachmentEvents: AssistantEvent
    data class RemoveAttachment(val index: Int): AssistantEvent
}
