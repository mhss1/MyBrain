package com.mhss.app.presentation

import com.mhss.app.domain.model.AiMessage

sealed interface AssistantEvent {
    data class SendMessage(val message: AiMessage): AssistantEvent
}
