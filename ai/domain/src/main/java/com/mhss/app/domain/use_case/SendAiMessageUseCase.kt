package com.mhss.app.domain.use_case

import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.network.NetworkResult
import com.mhss.app.domain.repository.AiApi
import com.mhss.app.preferences.domain.model.AiProvider
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class SendAiMessageUseCase(
    @Named("openaiApi") private val openai: AiApi,
    @Named("geminiApi") private val gemini: AiApi
) {
    suspend operator fun invoke(
        messages: List<AiMessage>,
        key: String,
        model: String,
        provider: AiProvider,
        baseURL: String = ""
    ): NetworkResult<AiMessage> {
        return try {
            if (key.isBlank()) return NetworkResult.InvalidKey
            when (provider) {
                AiProvider.OpenAI -> openai.sendMessage(
                    baseUrl = baseURL,
                    messages = messages,
                    model = model,
                    key = key
                )

                AiProvider.Gemini ->
                    gemini.sendMessage(
                        baseUrl = AiConstants.GEMINI_BASE_URL,
                        messages = messages,
                        model = model,
                        key = key
                    )

                else -> throw IllegalStateException("No AI provider is chosen")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError()
        }
    }
}