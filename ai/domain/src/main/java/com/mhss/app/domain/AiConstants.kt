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
    get() = """
        Summarize this note in bullet points.
        Respond with the summary only and don't say anything else.
        Use Markdown for formatting.
        Note content:
        $this
        Summary:
    """.trimIndent()

val String.autoFormatNotePrompt: String
    get() = """
        Format this note in a more readable way.
        Include headings, bullet points, and other formatting.
        Respond with the formatted note only and don't say anything else.
        Use Markdown for formatting.
        Note content:
        $this
        Formatted note:
    """.trimIndent()

val String.correctSpellingNotePrompt: String
    get() = """
        Correct the spelling and grammar errors in this note.
        Respond with the corrected note only and don't say anything else.
        Note content:
        $this
        Corrected note:
    """.trimIndent()