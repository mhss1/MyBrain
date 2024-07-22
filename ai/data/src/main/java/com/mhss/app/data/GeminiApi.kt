package com.mhss.app.data

import com.mhss.app.data.model.gemini.GeminiResponse
import com.mhss.app.data.model.gemini.text
import com.mhss.app.data.model.gemini.toAiMessage
import com.mhss.app.data.model.gemini.toGeminiRequestBody
import com.mhss.app.di.geminiApi
import com.mhss.app.di.namedIoDispatcher
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.NetworkResult
import com.mhss.app.domain.repository.AiApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named(geminiApi)
class GeminiApi(
    private val client: HttpClient,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : AiApi {

    override suspend fun sendPrompt(
        baseUrl: String,
        prompt: String,
        model: String,
        key: String
    ): NetworkResult {
        return withContext(ioDispatcher) {
            val result = client.post(baseUrl) {
                url {
                    appendPathSegments("models")
                    appendPathSegments("$model:generateContent")
                    parameters.append("key", key)
                }
                contentType(ContentType.Application.Json)
                setBody(prompt.toGeminiRequestBody())
            }.body<GeminiResponse>()
            if (result.error != null) {
                if (result.error.message.contains("API key")) {
                    NetworkResult.InvalidKey
                } else if (result.error.code in 400..499) {
                    NetworkResult.OtherError(result.error.message)
                } else {
                    NetworkResult.OtherError()
                }
            } else {
                NetworkResult.Success(result.text)
            }
        }
    }

    override suspend fun sendMessage(
        baseUrl: String,
        messages: List<AiMessage>,
        systemMessage: String,
        model: String,
        key: String
    ): NetworkResult {
        return withContext(ioDispatcher) {
            val result = client.post(baseUrl) {
                url {
                    appendPathSegments("models")
                    appendPathSegments("$model:generateContent")
                    parameters.append("key", key)
                }
                contentType(ContentType.Application.Json)
                setBody(messages.toGeminiRequestBody())
            }.body<GeminiResponse>()
            if (result.error != null) {
                if (result.error.message.contains("API key")) {
                    NetworkResult.InvalidKey
                } else if (result.error.code in 400..499) {
                    NetworkResult.OtherError(result.error.message)
                } else {
                    NetworkResult.OtherError()
                }
            } else {
                NetworkResult.Success(result.toAiMessage())
            }
        }
    }

}