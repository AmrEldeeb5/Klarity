package com.example.klarity.data.ai

import com.example.klarity.domain.repositories.AiProvider
import com.example.klarity.domain.repositories.AiSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

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

    /**
     * Sends [messages] (+ optional [system]) to the configured provider; returns text or throws
     * [AiException]. [AiSettings.temperature] (low by default) keeps a grounded assistant faithful;
     * a null temperature is omitted so the model uses its own default. Transient failures
     * (429 / 5xx / network) are retried with backoff.
     */
    suspend fun complete(
        settings: AiSettings,
        system: String?,
        messages: List<AiTurn>,
        maxTokens: Int = 2048,
    ): String {
        val apiKey = settings.apiKey?.takeIf { it.isNotBlank() } ?: throw AiException("No API key set.")
        val base = settings.baseUrl.trim().trimEnd('/').ifBlank { settings.provider.defaultBaseUrl }
        val temperature = settings.temperature
        return when (settings.provider) {
            AiProvider.ANTHROPIC -> anthropic(apiKey, settings.model, base, system, messages, maxTokens, temperature)
            // Groq speaks the OpenAI-compatible Chat Completions API.
            AiProvider.GROQ, AiProvider.OPENAI -> openAi(apiKey, settings.model, base, system, messages, maxTokens, temperature)
        }
    }

    /**
     * Streaming counterpart to [complete] — emits incremental text deltas as the model produces
     * them (Server-Sent Events). The SSE framing is shared across providers (both put content on
     * `data:` lines); only the per-line JSON differs, handled by [StreamParsing]. Throws
     * [AiException] on a non-success status or connection failure. No retry here — the caller can
     * fall back to [complete] (which retries) if nothing was emitted.
     */
    fun completeStream(
        settings: AiSettings,
        system: String?,
        messages: List<AiTurn>,
        maxTokens: Int = 2048,
    ): Flow<String> = flow {
        val apiKey = settings.apiKey?.takeIf { it.isNotBlank() } ?: throw AiException("No API key set.")
        val base = settings.baseUrl.trim().trimEnd('/').ifBlank { settings.provider.defaultBaseUrl }
        val temperature = settings.temperature
        val isAnthropic = settings.provider == AiProvider.ANTHROPIC

        val statement = http.preparePost(if (isAnthropic) "$base/v1/messages" else "$base/chat/completions") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, "text/event-stream")
            if (isAnthropic) {
                header("x-api-key", apiKey)
                header("anthropic-version", ANTHROPIC_VERSION)
                setBody(AnthropicRequest(model = settings.model, maxTokens = maxTokens, temperature = temperature, system = system, messages = messages, stream = true))
            } else {
                header("Authorization", "Bearer $apiKey")
                val full = buildList {
                    if (!system.isNullOrBlank()) add(AiTurn("system", system))
                    addAll(messages)
                }
                setBody(OpenAiRequest(model = settings.model, messages = full, maxTokens = maxTokens, temperature = temperature, stream = true))
            }
        }

        try {
            statement.execute { response ->
                if (!response.status.isSuccess()) throw AiException(errorMessage(response))
                val channel = response.bodyAsChannel()
                while (true) {
                    val line = channel.readUTF8Line() ?: break
                    if (!line.startsWith("data:")) continue // skip "event:" / comment / blank lines
                    val data = line.substring(5).trim()
                    if (data.isEmpty()) continue
                    if (data == StreamParsing.DONE) break
                    val delta = if (isAnthropic) StreamParsing.anthropicDelta(data) else StreamParsing.openAiDelta(data)
                    if (delta != null) emit(delta)
                }
            }
        } catch (e: AiException) {
            throw e
        } catch (e: Exception) {
            throw AiException("Couldn't reach the AI service — check the base URL and your connection.")
        }
    }

    /**
     * Non-streaming completion that offers the model [tools]. Returns [AiResult.Actions] when the
     * model decides to call one or more tools (the caller confirms + executes them), or
     * [AiResult.Text] for a plain answer. Used for the agentic path; see [AiTools].
     */
    suspend fun completeWithTools(
        settings: AiSettings,
        system: String?,
        messages: List<AiTurn>,
        tools: JsonArray,
        maxTokens: Int = 2048,
    ): AiResult {
        val apiKey = settings.apiKey?.takeIf { it.isNotBlank() } ?: throw AiException("No API key set.")
        val base = settings.baseUrl.trim().trimEnd('/').ifBlank { settings.provider.defaultBaseUrl }
        val temperature = settings.temperature
        return when (settings.provider) {
            AiProvider.ANTHROPIC -> anthropicTools(apiKey, settings.model, base, system, messages, maxTokens, temperature, tools)
            AiProvider.GROQ, AiProvider.OPENAI -> openAiTools(apiKey, settings.model, base, system, messages, maxTokens, temperature, tools)
        }
    }

    private suspend fun anthropicTools(
        apiKey: String, model: String, base: String, system: String?, messages: List<AiTurn>,
        maxTokens: Int, temperature: Double?, tools: JsonArray,
    ): AiResult {
        val response = postWithRetry("$base/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(AnthropicRequest(model = model, maxTokens = maxTokens, temperature = temperature, system = system, messages = messages, tools = tools))
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: AnthropicResponse = decode(response)
        if (body.stopReason == "refusal") throw AiException("The model declined to answer that request.")
        val text = body.content.filter { it.type == "text" }.joinToString("") { it.text }.trim()
        val calls = body.content.filter { it.type == "tool_use" }
            .map { ToolCall(id = it.id, name = it.name, args = it.input ?: EMPTY_ARGS) }
        return result(text, calls)
    }

    private suspend fun openAiTools(
        apiKey: String, model: String, base: String, system: String?, messages: List<AiTurn>,
        maxTokens: Int, temperature: Double?, tools: JsonArray,
    ): AiResult {
        val full = buildList {
            if (!system.isNullOrBlank()) add(AiTurn("system", system))
            addAll(messages)
        }
        val response = postWithRetry("$base/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            setBody(OpenAiRequest(model = model, messages = full, maxTokens = maxTokens, temperature = temperature, tools = tools))
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: OpenAiResponse = decode(response)
        val message = body.choices.firstOrNull()?.message
        val text = message?.content?.trim().orEmpty()
        val calls = message?.toolCalls.orEmpty().map {
            ToolCall(id = it.id, name = it.function.name, args = parseArgs(it.function.arguments))
        }
        return result(text, calls)
    }

    /** Folds extracted text + tool calls into an [AiResult], erroring only on a wholly empty reply. */
    private fun result(text: String, calls: List<ToolCall>): AiResult = when {
        calls.isNotEmpty() -> AiResult.Actions(text.ifBlank { null }, calls)
        text.isNotEmpty() -> AiResult.Text(text)
        else -> throw AiException("The model returned an empty response.")
    }

    /** Parses an OpenAI tool-call `arguments` string (JSON object) — empty/garbage becomes `{}`. */
    private fun parseArgs(arguments: String): JsonObject =
        runCatching { json.parseToJsonElement(arguments).jsonObject }.getOrDefault(EMPTY_ARGS)

    /**
     * Fetches the model IDs the given key can use, from the provider's `/models` endpoint (both
     * Anthropic and OpenAI-compatible APIs expose this with a `{data:[{id}]}` shape).
     */
    suspend fun listModels(provider: AiProvider, apiKey: String, baseUrl: String): List<String> {
        val base = baseUrl.trim().trimEnd('/').ifBlank { provider.defaultBaseUrl }
        val url = when (provider) {
            AiProvider.ANTHROPIC -> "$base/v1/models"
            AiProvider.GROQ, AiProvider.OPENAI -> "$base/models"
        }
        val response = try {
            http.get(url) {
                when (provider) {
                    AiProvider.ANTHROPIC -> {
                        header("x-api-key", apiKey)
                        header("anthropic-version", ANTHROPIC_VERSION)
                    }
                    AiProvider.GROQ, AiProvider.OPENAI -> header("Authorization", "Bearer $apiKey")
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
        temperature: Double?,
    ): String {
        val response = postWithRetry("$base/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(AnthropicRequest(model = model, maxTokens = maxTokens, temperature = temperature, system = system, messages = messages))
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
        temperature: Double?,
    ): String {
        val full = buildList {
            if (!system.isNullOrBlank()) add(AiTurn("system", system))
            addAll(messages)
        }
        val response = postWithRetry("$base/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            setBody(OpenAiRequest(model = model, messages = full, maxTokens = maxTokens, temperature = temperature))
        }
        if (!response.status.isSuccess()) throw AiException(errorMessage(response))
        val body: OpenAiResponse = decode(response)
        return body.choices.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw AiException("The model returned an empty response.")
    }

    /**
     * POSTs with retry on transient failures — 429, 5xx, and network errors — using exponential
     * backoff. Non-retryable responses (and the final retryable one) are returned as-is for the
     * caller's normal status handling; only an exhausted network failure throws.
     */
    private suspend fun postWithRetry(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit): HttpResponse {
        repeat(MAX_ATTEMPTS) { attempt ->
            val response = try {
                http.post(url) {
                    contentType(ContentType.Application.Json)
                    block()
                }
            } catch (e: Exception) {
                if (attempt < MAX_ATTEMPTS - 1) {
                    delay(backoffMillis(attempt))
                    return@repeat
                }
                throw AiException("Couldn't reach the AI service — check the base URL and your connection.")
            }
            val status = response.status.value
            val retryable = status == 429 || status in 500..599
            if (retryable && attempt < MAX_ATTEMPTS - 1) {
                delay(backoffMillis(attempt))
                return@repeat
            }
            return response
        }
        // Unreachable: the loop either returns a response or throws on the final network failure.
        throw AiException("Couldn't reach the AI service — try again shortly.")
    }

    /** Exponential backoff: 400ms, 800ms, … between attempts. */
    private fun backoffMillis(attempt: Int): Long = RETRY_BASE_MILLIS shl attempt

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

        /** Total tries (1 initial + 2 retries) for transient 429 / 5xx / network failures. */
        const val MAX_ATTEMPTS = 3
        const val RETRY_BASE_MILLIS = 400L

        /** Shared empty arguments object for tool calls that carry none. */
        val EMPTY_ARGS = JsonObject(emptyMap())
    }
}

// ── Wire models ──────────────────────────────────────────────────────────────

@Serializable
private data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    // null default + the client's encodeDefaults=false means "Auto" (null) is omitted from the body,
    // while a set value is sent — exactly the behaviour we want.
    val temperature: Double? = null,
    val system: String? = null,
    val messages: List<AiTurn>,
    // Default false → omitted (encodeDefaults=false) for the non-streaming complete() path.
    val stream: Boolean = false,
    val tools: JsonArray? = null,
)

@Serializable
private data class AnthropicResponse(
    val content: List<AnthropicBlock> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
private data class AnthropicBlock(
    val type: String,
    val text: String = "",
    // Present on `tool_use` blocks:
    val id: String = "",
    val name: String = "",
    val input: JsonObject? = null,
)

@Serializable
private data class OpenAiRequest(
    val model: String,
    val messages: List<AiTurn>,
    @SerialName("max_tokens") val maxTokens: Int,
    val temperature: Double? = null,
    val stream: Boolean = false,
    val tools: JsonArray? = null,
)

@Serializable
private data class OpenAiResponse(val choices: List<OpenAiChoice> = emptyList())

@Serializable
private data class OpenAiChoice(val message: OpenAiMessage? = null)

@Serializable
private data class OpenAiMessage(
    val role: String = "",
    // content is null when the model returns only tool_calls.
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiToolCall> = emptyList(),
)

@Serializable
private data class OpenAiToolCall(val id: String = "", val function: OpenAiFunctionCall = OpenAiFunctionCall())

@Serializable
private data class OpenAiFunctionCall(val name: String = "", val arguments: String = "")

@Serializable
private data class ApiErrorEnvelope(val error: ApiError? = null)

@Serializable
private data class ApiError(val message: String = "")

@Serializable
private data class ModelsList(val data: List<ModelEntry> = emptyList())

@Serializable
private data class ModelEntry(val id: String = "")
