package com.example.klarity.domain.repositories

import kotlinx.coroutines.flow.Flow

/**
 * AI provider the assistant talks to. [ANTHROPIC] uses the native Messages API; [OPENAI] uses the
 * OpenAI-compatible Chat Completions API, which most providers and local servers also speak — so a
 * custom [AiSettings.baseUrl] + free-text model covers OpenAI, OpenRouter, Groq, Ollama, LM Studio,
 * DeepSeek, Together, etc.
 */
enum class AiProvider(
    val label: String,
    val short: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val keyHint: String,
    val baseUrlHint: String,
    val exampleModels: List<String>,
) {
    ANTHROPIC(
        label = "Claude (Anthropic)",
        short = "Claude",
        defaultBaseUrl = "https://api.anthropic.com",
        defaultModel = "claude-haiku-4-5",
        keyHint = "sk-ant-…",
        baseUrlHint = "https://api.anthropic.com",
        exampleModels = listOf("claude-haiku-4-5", "claude-sonnet-4-6", "claude-opus-4-8"),
    ),
    OPENAI(
        label = "OpenAI-compatible",
        short = "Custom AI",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        keyHint = "sk-… / gsk_…",
        baseUrlHint = "OpenAI default · Groq https://api.groq.com/openai/v1 · OpenRouter https://openrouter.ai/api/v1 · Ollama http://localhost:11434/v1",
        exampleModels = listOf("gpt-4o-mini", "llama-3.3-70b-versatile", "deepseek-chat", "moonshotai/kimi-k2-instruct-0905"),
    );

    companion object {
        fun fromId(id: String?): AiProvider = entries.firstOrNull { it.name == id } ?: ANTHROPIC
    }
}

/** Locally-stored AI assistant configuration. */
data class AiSettings(
    val provider: AiProvider = AiProvider.ANTHROPIC,
    val apiKey: String? = null,
    val model: String = provider.defaultModel,
    val baseUrl: String = provider.defaultBaseUrl,
) {
    val enabled: Boolean get() = !apiKey.isNullOrBlank()
}

/** Persists local app settings (key/value backed by SQLDelight). */
interface SettingsRepository {
    /** Reactive stream of the current AI settings. */
    fun settings(): Flow<AiSettings>

    /** One-shot read of the current AI settings (for building a request). */
    suspend fun current(): AiSettings

    /** Save the full AI configuration. A null/blank [apiKey] disables the AI (clears the key). */
    suspend fun save(provider: AiProvider, apiKey: String?, model: String, baseUrl: String)
}
