package com.example.klarity.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parsers for the Server-Sent Events emitted by the streaming chat APIs. Each function takes the
 * JSON payload of a single `data:` line and returns the incremental text it carries (or null for
 * the many control/keep-alive events that carry no text). Pure — no I/O — so the delta extraction
 * is unit-testable without a network. The SSE framing itself (splitting `data:` lines) lives in
 * [AiService].
 */
object StreamParsing {

    /** Sentinel the OpenAI-compatible APIs send to mark the end of the stream. */
    const val DONE = "[DONE]"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Incremental text from one OpenAI Chat Completions stream chunk (`choices[].delta.content`). */
    fun openAiDelta(data: String): String? {
        if (data == DONE) return null
        val chunk = runCatching { json.decodeFromString(OpenAiChunk.serializer(), data) }.getOrNull() ?: return null
        return chunk.choices.firstOrNull()?.delta?.content?.takeIf { it.isNotEmpty() }
    }

    /** Incremental text from one Anthropic Messages stream event (text_delta blocks only). */
    fun anthropicDelta(data: String): String? {
        val event = runCatching { json.decodeFromString(AnthropicEvent.serializer(), data) }.getOrNull() ?: return null
        if (event.type != "content_block_delta") return null
        val delta = event.delta ?: return null
        if (delta.type != "text_delta") return null
        return delta.text?.takeIf { it.isNotEmpty() }
    }

    // ── Wire models (streaming) ──────────────────────────────────────────────
    @Serializable
    private data class OpenAiChunk(val choices: List<OpenAiStreamChoice> = emptyList())

    @Serializable
    private data class OpenAiStreamChoice(val delta: OpenAiStreamDelta? = null)

    @Serializable
    private data class OpenAiStreamDelta(val content: String? = null)

    @Serializable
    private data class AnthropicEvent(
        val type: String = "",
        val delta: AnthropicEventDelta? = null,
    )

    @Serializable
    private data class AnthropicEventDelta(
        val type: String = "",
        val text: String? = null,
        @SerialName("stop_reason") val stopReason: String? = null,
    )
}
