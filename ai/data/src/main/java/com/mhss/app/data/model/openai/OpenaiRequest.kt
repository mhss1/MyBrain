package com.mhss.app.data.model.openai

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