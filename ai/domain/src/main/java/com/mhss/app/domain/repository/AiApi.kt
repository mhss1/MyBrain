package com.mhss.app.domain.repository

import com.mhss.app.domain.model.AiMessage

interface AiApi {

    suspend fun sendPrompt(
        baseUrl: String,
        prompt: String,
        model: String,
        key: String
    ): String

    suspend fun sendMessage(
        baseUrl: String,
        messages: List<AiMessage>,
        systemMessage: String,
        model: String,
        key: String
    ): AiMessage

}