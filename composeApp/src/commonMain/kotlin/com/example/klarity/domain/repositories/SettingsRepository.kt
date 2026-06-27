package com.example.klarity.domain.repositories

import kotlinx.coroutines.flow.Flow

/**
 * AI provider the assistant talks to. [ANTHROPIC] uses the native Messages API; [GROQ] and [OPENAI]
 * both speak the OpenAI-compatible Chat Completions API. [GROQ] is preconfigured for the Groq cloud
 * (fixed base URL, curated model list); [OPENAI] keeps a free-text [AiSettings.baseUrl] + model so it
 * also covers OpenAI, OpenRouter, Ollama, LM Studio, DeepSeek, Together, etc.
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
    GROQ(
        label = "Groq",
        short = "Groq",
        defaultBaseUrl = "https://api.groq.com/openai/v1",
        defaultModel = "llama-3.3-70b-versatile",
        keyHint = "gsk_…",
        baseUrlHint = "https://api.groq.com/openai/v1",
        exampleModels = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "qwen/qwen3-32b",
            "moonshotai/kimi-k2-instruct-0905",
        ),
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

/** Default sampling temperature — low so a workspace-grounded assistant stays faithful, not creative. */
const val DEFAULT_AI_TEMPERATURE = 0.3

/** Locally-stored AI assistant configuration. */
data class AiSettings(
    val provider: AiProvider = AiProvider.ANTHROPIC,
    val apiKey: String? = null,
    val model: String = provider.defaultModel,
    val baseUrl: String = provider.defaultBaseUrl,
    /** Sampling temperature; `null` means "Auto" — omit it so the model uses its own default
     *  (required by reasoning models that reject any fixed temperature). */
    val temperature: Double? = DEFAULT_AI_TEMPERATURE,
    /** When true, Lou may propose workspace actions (tool-calling). Turn off for models without
     *  tool support, or to keep Lou read-only (which also re-enables streaming answers). */
    val actionsEnabled: Boolean = true,
) {
    val enabled: Boolean get() = !apiKey.isNullOrBlank()
}

/** Persists local app settings (key/value backed by SQLDelight). */
interface SettingsRepository {
    /** Reactive stream of the current AI settings. */
    fun settings(): Flow<AiSettings>

    /** One-shot read of the current AI settings (for building a request). */
    suspend fun current(): AiSettings

    /**
     * Save the full AI configuration. A null/blank [apiKey] disables the AI (clears the key).
     * A null [temperature] persists as "Auto" (the model's own default is used at request time).
     */
    suspend fun save(
        provider: AiProvider,
        apiKey: String?,
        model: String,
        baseUrl: String,
        temperature: Double?,
        actionsEnabled: Boolean,
    )
}
