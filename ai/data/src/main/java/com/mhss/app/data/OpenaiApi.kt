package com.mhss.app.data

import com.mhss.app.data.model.openai.OpenaiMessage
import com.mhss.app.data.model.openai.OpenaiMessageRequestBody
import com.mhss.app.data.model.openai.OpenaiResponse
import com.mhss.app.data.model.openai.toAiMessage
import com.mhss.app.data.model.openai.toOpenAiRequestBody
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.network.NetworkResult
import com.mhss.app.domain.repository.AiApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
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
@Named("openaiApi")
class OpenaiApi(
    private val client: HttpClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : AiApi {
    override suspend fun sendPrompt(baseUrl: String, prompt: String, model: String, key: String)
            : NetworkResult<String> {
        return withContext(ioDispatcher) {
            val result = client.post(baseUrl) {
                url {
                    appendPathSegments("chat", "completions")
                }
                contentType(ContentType.Application.Json)
                bearerAuth(key)
                setBody(
                    OpenaiMessageRequestBody(
                        model = model,
                        messages = listOf(
                            OpenaiMessage(
                                content = prompt,
                                role = NetworkConstants.OPENAI_MESSAGE_USER_TYPE
                            )
                        )

                    )
                )
            }.body<OpenaiResponse>()
            if (result.error != null) {
                if (result.error.message.contains("API key")) {
                    NetworkResult.InvalidKey
                } else {
                    NetworkResult.OtherError(result.error.message)
                }
            } else {
                NetworkResult.Success(result.choices!!.first().message.content)
            }
        }
    }

    override suspend fun sendMessage(
        baseUrl: String,
        messages: List<AiMessage>,
        model: String,
        key: String
    ): NetworkResult<AiMessage> {
        return withContext(ioDispatcher) {
            val result = client.post(baseUrl) {
                url {
                    appendPathSegments("chat", "completions")
                }
                contentType(ContentType.Application.Json)
                bearerAuth(key)
                setBody(messages.toOpenAiRequestBody(model))
            }.body<OpenaiResponse>()
            if (result.error != null) {
                if (result.error.message.contains("API key")) {
                    NetworkResult.InvalidKey
                } else {
                    NetworkResult.OtherError(result.error.message)
                }
            } else {
                NetworkResult.Success(result.choices!!.first().message.toAiMessage())
            }
        }
    }


}