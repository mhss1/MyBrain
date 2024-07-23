package com.mhss.app.domain.repository

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.network.NetworkResult

interface AiApi {

    suspend fun sendPrompt(
        baseUrl: String,
        prompt: String,
        model: String,
        key: String
    ): NetworkResult<String>

    suspend fun sendMessage(
        baseUrl: String,
        messages: List<AiMessage>,
        systemMessage: String,
        model: String,
        key: String
    ): NetworkResult<AiMessage>
}