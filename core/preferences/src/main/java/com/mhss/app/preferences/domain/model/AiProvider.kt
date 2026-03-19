package com.mhss.app.preferences.domain.model

import com.mhss.app.preferences.PrefsConstants

enum class AiProvider(
    val id: Int,
    val keyPref: String? = null,
    val modelPref: String? = null,
    val defaultModel: String? = null,
    val keyInfoUrl: String? = null,
    val modelsInfoUrl: String? = null,
    val supportsCustomUrl: Boolean = false,
    val requiresCustomUrl: Boolean = false,
    val isLocalProvider: Boolean = false,
    val customUrlPref: String? = null,
    val customUrlEnabledPref: String? = null,
    val defaultBaseUrl: String? = null
) {
    None(id = 0),
    OpenAI(
        id = 2,
        keyPref = PrefsConstants.OPENAI_KEY,
        modelPref = PrefsConstants.OPENAI_MODEL_KEY,
        defaultModel = "gpt-5.4",
        keyInfoUrl = "https://platform.openai.com/api-keys",
        modelsInfoUrl = "https://platform.openai.com/docs/models",
        supportsCustomUrl = true,
        customUrlPref = PrefsConstants.OPENAI_URL_KEY,
        customUrlEnabledPref = PrefsConstants.OPENAI_USE_URL_KEY,
        defaultBaseUrl = "https://api.openai.com/v1"
    ),
    Gemini(
        id = 1,
        keyPref = PrefsConstants.GEMINI_KEY,
        modelPref = PrefsConstants.GEMINI_MODEL_KEY,
        defaultModel = "gemini-3.1-pro-preview",
        keyInfoUrl = "https://aistudio.google.com/apikey",
        modelsInfoUrl = "https://ai.google.dev/gemini-api/docs/models"
    ),
    Anthropic(
        id = 3,
        keyPref = PrefsConstants.ANTHROPIC_KEY,
        modelPref = PrefsConstants.ANTHROPIC_MODEL_KEY,
        defaultModel = "claude-opus-4-6",
        keyInfoUrl = "https://console.anthropic.com/settings/keys",
        modelsInfoUrl = "https://platform.claude.com/docs/en/about-claude/models/overview"
    ),
    OpenRouter(
        id = 4,
        keyPref = PrefsConstants.OPEN_ROUTER_KEY,
        modelPref = PrefsConstants.OPEN_ROUTER_MODEL_KEY,
        defaultModel = "openrouter/auto",
        keyInfoUrl = "https://openrouter.ai/keys",
        modelsInfoUrl = "https://openrouter.ai/models"
    ),
    LmStudio(
        id = 5,
        keyPref = null,
        modelPref = PrefsConstants.LM_STUDIO_MODEL_KEY,
        defaultModel = "openai/gpt-oss-20b",
        keyInfoUrl = "",
        modelsInfoUrl = "https://lmstudio.ai/models",
        supportsCustomUrl = true,
        requiresCustomUrl = true,
        isLocalProvider = true,
        customUrlPref = PrefsConstants.LM_STUDIO_URL_KEY,
        defaultBaseUrl = "http://192.168.1.100:1234"
    ),
    Ollama(
        id = 6,
        keyPref = null,
        modelPref = PrefsConstants.OLLAMA_MODEL_KEY,
        defaultModel = "gpt-oss:latest",
        keyInfoUrl = "",
        modelsInfoUrl = "https://ollama.com/library",
        supportsCustomUrl = true,
        requiresCustomUrl = true,
        isLocalProvider = true,
        customUrlPref = PrefsConstants.OLLAMA_URL_KEY,
        defaultBaseUrl = "http://192.168.1.100:11434"
    );
}

fun Int.toAiProvider() = AiProvider.entries.firstOrNull { entry -> entry.id == this } ?: AiProvider.None

val AiProvider.keyPrefsKey: PrefsKey<String>?
    get() = keyPref?.let(::stringPreferencesKey)

val AiProvider.modelPrefsKey: PrefsKey<String>?
    get() = modelPref?.let(::stringPreferencesKey)

val AiProvider.customUrlPrefsKey: PrefsKey<String>?
    get() = customUrlPref?.let(::stringPreferencesKey)

val AiProvider.customUrlEnabledPrefsKey: PrefsKey<Boolean>?
    get() = customUrlEnabledPref?.let(::booleanPreferencesKey)