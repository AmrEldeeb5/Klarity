package com.example.klarity.data.ai

import com.example.klarity.domain.repositories.AiProvider
import com.example.klarity.domain.repositories.AiSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One conversation turn. [role] is "user", "assistant", or (OpenAI only) "system". */
@Serializable
data class AiTurn(val role: String, val content: String)

/** Raised on any AI call failure; [message] is safe to show to the user. */
class AiException(message: String) : Exception(message)

/**
 * Talks to an LLM over raw HTTP (Ktor) so it works in commonMain (Android + desktop) with no
 * JVM-only SDK. Supports the native Anthropic Messages API and the OpenAI-compatible Chat
 * Completions API — the latter, with a custom base URL + model, covers most providers and local
 * servers. Non-streaming.
 */
class AiService(private val http: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    /** Sends [messages] (+ optional [system]) to the configured provider; returns text or throws [AiException]. */
    suspend fun complete(
        settings: AiSettings,
        system: String?,
        messages: List<AiTurn>,
        maxTokens: Int = 2048,
    ): String {
        val apiKey = settings.apiKey?.takeIf { it.isNotBlank() } ?: throw AiException("No API key set.")
        val base = settings.baseUrl.trim().trimEnd('/').ifBlank { settings.provider.defaultBaseUrl }
        return when (settings.provider) {
            AiProvider.ANTHROPIC -> anthropic(apiKey, settings.model, base, system, messages, maxTokens)
            AiProvider.OPENAI -> openAi(apiKey, settings.model, base, system, messages, maxTokens)
        }
    }

    /**
     * Fetches the model IDs the given key can use, from the provider's `/models` endpoint (both
     * Anthropic and OpenAI-compatible APIs expose this with a `{data:[{id}]}` shape).
     */
    suspend fun listModels(provider: AiProvider, apiKey: String, baseUrl: String): List<String> {
        val base = baseUrl.trim().trimEnd('/').ifBlank { provider.defaultBaseUrl }
        val url = when (provider) {
            AiProvider.ANTHROPIC -> "$base/v1/models"
            AiProvider.OPENAI -> "$base/models"
        }
        val response = try {
            http.get(url) {
                when (provider) {
                    AiProvider.ANTHROPIC -> {
                        header("x-api-key", apiKey)
                        header("anthropic-version", ANTHROPIC_VERSION)
                    }
                    AiProvider.OPENAI -> header("Authorization", "Bearer $apiKey")
                }
            }
        } catch (e: Exception) {
            throw AiException("Couldn't reach the provider — check the base URL and your connection.")
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: ModelsList = decode(response)
        return body.data.map { it.id }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // ── Anthropic Messages API ───────────────────────────────────────────────
    private suspend fun anthropic(
        apiKey: String,
        model: String,
        base: String,
        system: String?,
        messages: List<AiTurn>,
        maxTokens: Int,
    ): String {
        val response = request("$base/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(AnthropicRequest(model = model, maxTokens = maxTokens, system = system, messages = messages))
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: AnthropicResponse = decode(response)
        if (body.stopReason == "refusal") throw AiException("The model declined to answer that request.")
        return body.content.firstOrNull { it.type == "text" }?.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AiException("The model returned an empty response.")
    }

    // ── OpenAI-compatible Chat Completions API ───────────────────────────────
    private suspend fun openAi(
        apiKey: String,
        model: String,
        base: String,
        system: String?,
        messages: List<AiTurn>,
        maxTokens: Int,
    ): String {
        val full = buildList {
            if (!system.isNullOrBlank()) add(AiTurn("system", system))
            addAll(messages)
        }
        val response = request("$base/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            setBody(OpenAiRequest(model = model, messages = full, maxTokens = maxTokens))
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: OpenAiResponse = decode(response)
        return body.choices.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AiException("The model returned an empty response.")
    }

    private suspend fun request(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit): HttpResponse =
        try {
            http.post(url) {
                contentType(ContentType.Application.Json)
                block()
            }
        } catch (e: Exception) {
            throw AiException("Couldn't reach the AI service — check the base URL and your connection.")
        }

    private suspend inline fun <reified T> decode(response: HttpResponse): T =
        try {
            response.body()
        } catch (e: Exception) {
            throw AiException("The AI service returned an unexpected response.")
        }

    private suspend fun errorMessage(response: HttpResponse): String {
        val raw = runCatching { response.bodyAsText() }.getOrNull()
        // Both Anthropic and OpenAI nest the message at {"error":{"message":...}}.
        val apiMessage = raw?.let {
            runCatching { json.decodeFromString(ApiErrorEnvelope.serializer(), it).error?.message }.getOrNull()
        }?.takeIf { it.isNotBlank() }
        return when (response.status.value) {
            401 -> "Invalid API key — check it in Settings."
            403 -> "This API key can't access the selected model."
            404 -> "Not found — check the model and base URL in Settings."
            429 -> "Rate limited — wait a moment and try again."
            in 500..599 -> "The AI service is having trouble — try again shortly."
            else -> apiMessage ?: "Request failed (HTTP ${response.status.value})."
        }
    }

    private companion object {
        const val ANTHROPIC_VERSION = "2023-06-01"
    }
}

// ── Wire models ──────────────────────────────────────────────────────────────

@Serializable
private data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String? = null,
    val messages: List<AiTurn>,
)

@Serializable
private data class AnthropicResponse(
    val content: List<AnthropicBlock> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
private data class AnthropicBlock(val type: String, val text: String = "")

@Serializable
private data class OpenAiRequest(
    val model: String,
    val messages: List<AiTurn>,
    @SerialName("max_tokens") val maxTokens: Int,
)

@Serializable
private data class OpenAiResponse(val choices: List<OpenAiChoice> = emptyList())

@Serializable
private data class OpenAiChoice(val message: OpenAiMessage? = null)

@Serializable
private data class OpenAiMessage(val role: String = "", val content: String = "")

@Serializable
private data class ApiErrorEnvelope(val error: ApiError? = null)

@Serializable
private data class ApiError(val message: String = "")

@Serializable
private data class ModelsList(val data: List<ModelEntry> = emptyList())

@Serializable
private data class ModelEntry(val id: String = "")
