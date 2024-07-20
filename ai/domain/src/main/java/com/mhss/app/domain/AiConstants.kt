package com.mhss.app.domain

object AiConstants {
    const val OPENAI_BASE_URL = "https://api.openai.com/v1"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1"

    const val OPENAI_DEFAULT_MODEL = "gpt-4o"
    const val GEMINI_DEFAULT_MODEL = "gemini-1.5-pro"

    const val GEMINI_KEY_INFO_URL = "https://ai.google.dev/gemini-api/docs/api-key"
    const val OPENAI_KEY_INFO_URL = "https://platform.openai.com/api-keys"

    const val GEMINI_MODELS_INFO_URL = "https://ai.google.dev/gemini-api/docs/models/gemini"
    const val OPENAI_MODELS_INFO_URL = "https://platform.openai.com/docs/models"

}

val String.summarizeNotePrompt: String
    get() = "Summarize this note in a few bullet points. Respond with the summary only and don't say anything else.\nNote content:\n$this\nSummary:"